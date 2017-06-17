# Rewriting Guava problems in Github

## BigQuery

I've created a project in my Google Cloud account called `github-166820`.
Substitute your own project name for `myproject` in the queries below.

### Step 1: Identify all Java sources

In options, save the results of this query to: `myproject:gradle_summit.java_files`.
You will have to allow large results as well. This is a fairly cheap query (336 GB).

```sql
SELECT * FROM [bigquery-public-data:github_repos.files]
WHERE RIGHT(path, 5) = '.java'
```

### Step 2: Move Java source file contents

This will blow past the monthly free tier and eat into your $300 credits.
It cost me approximately $6 (1.94 TB) when I ran it on May 31.

```sql
SELECT *
FROM [bigquery-public-data:github_repos.contents]
WHERE id IN (SELECT id FROM [myproject:gradle_summit.java_files])
```

Save the results of this query to: `myproject:gradle_summit.java_file_contents`.
Again, allow large results as well.

### Step 3: Identify Guava related sources

Things start getting cheaper now because we are dealing with smaller and smaller datasets
(95.6 GB).

```sql
SELECT repo_name, path, content FROM [myproject:gradle_summit.java_file_contents] contents
INNER JOIN [myproject:gradle_summit.java_files] files ON files.id = contents.id
WHERE content CONTAINS 'import com.google.common'
```

Notice we are going to join just enough data from `gradle_summit.java_files`
and `gradle_summit:java_file_contents` in order to be able to construct our PRs.

Save the result to `myproject:gradle_summit.java_file_contents_guava`.

Through Step 3, we have cut down the size of the initial BigQuery public dataset
from 1.94 TB to around 25 GB. Much more manageable!

Also, I promise, this is the last simple text searching we will need to do!

There were 2,683,053 Java sources referring to Guava on May 31, 2017!

### Creating a Dataproc Cluster:

Initially followed these instructions, but the public dataproc-initialization-actions
bucket is fine:

https://cloudacademy.com/blog/big-data-using-apache-spark-and-zeppelin-on-google-cloud-dataproc/
https://medium.com/google-cloud/dataproc-spark-cluster-on-gcp-in-minutes-3843b8d8c5f8

In advanced settings, install the initializer:

`gs://dataproc-initialization-actions/zeppelin/zeppelin.sh`

Add the sample Atlas collector service from spring-metrics to the master node:

1) `gcloud compute scp atlas-collector/build/libs/atlas-collector.jar cluster-1-m:~/atlas-collector.jar --compress`
2) SSH into the master
3) `nohup java -jar atlas-collector.jar > /dev/null 2>&1&`
4) To prove Atlas is running: `curl -s 'http://localhost:7101/api/v1/tags'`

Follow the instructions here to set up a socks5 SSH tunnel:

https://cloud.google.com/dataproc/docs/concepts/cluster-web-interfaces

You can open your local browser to `localhost:8080` to access Zeppelin.
`localhost:4040` has the Spark UI

On the Spark interpreter configuration, set `spark.executor.memory` to 4g. Otherwise,
memory pressure starts to build up on parsing, slowing it down substantially.

### Metrics on parsing

http://localhost:7101/api/v1/graph?q=name,parse,:eq,statistic,count,:eq,:and,source+file+count,:legend,1,:axis,name,parse,:eq,statistic,totalTime,:eq,:and,name,parse,:eq,statistic,count,:eq,:and,:div,average+latency,:legend,2,:lw&tz=US/Central&l=0&title=Rewrite+Source+Parsing&ylabel.0=seconds&ylabel.1=sources/second&s=e-20m

Parsing tends to take around 0.12s per Java source file.
Rate of about 6.5 sources per second per core = 25 sources per second per node.

2,590,078/2,687,984 Java sources were parseable by Rewrite = 96.3%.