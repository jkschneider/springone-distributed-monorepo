package com.gradle.summit.rewrite

import com.netflix.rewrite.ast.Tr
import com.netflix.rewrite.parse.OracleJdkParser
import org.apache.commons.csv.CSVFormat
import java.io.File
import java.net.URL
import java.util.stream.Stream
import java.util.stream.StreamSupport
import java.util.zip.GZIPInputStream

object Step2FindBreakages {
    @JvmStatic
    fun main(args: Array<String>) {
        val parser = OracleJdkParser(listOf(guava("18.0").toPath()))
        val issues = File(".rewrite/issues.csv")

        Stream.of(*File(".rewrite/bucket").listFiles())
                .filter { it.name != ".DS_Store" }
                .forEach { f ->
                    println("Reading ${f.name}")
                    GZIPInputStream(f.inputStream()).use { gz ->
                        StreamSupport.stream(CSVFormat.EXCEL.parse(gz.reader()).spliterator(), false).skip(1).map {
                            JavaSourceRow(it[0], it[1], it[2])
                        }.forEach { (repo, path, source) ->
                            try {
                                val ast = parser.parse(source)
                                val problems = hasGuavaBreakage(ast)
                                println(problems.joinToString("/").let { if(it.isEmpty()) "<None>" else it } + ",$repo,$path")
                                if(problems.isNotEmpty()) {
                                    issues.appendText("$repo,$path,${problems.joinToString("/")}\n")
                                }
                            } catch(t: Throwable) {
                                println("<Unparseable>,$repo,$path")
                            }
                        }
                    }
                }
    }
    
    fun hasGuavaBreakage(cu: Tr.CompilationUnit): List<String> {
        val problems = mutableListOf<String>()
        
        if (cu.hasType("com.google.common.util.concurrent.FutureFallback"))
            problems.add("FutureFallback")
        if (cu.hasType("com.google.common.io.InputSupplier"))
            problems.add("InputSupplier")
        if (cu.hasType("com.google.common.io.OutputSupplier"))
            problems.add("OutputSupplier")
        if (cu.hasType("com.google.common.collect.MapConstraints"))
            problems.add("MapConstraints")
        if (!cu.findMethodCalls("com.google.common.base.Objects firstNonNull(..)").isEmpty())
            problems.add("Objects.firstNonNull")
        if (!cu.findMethodCalls("com.google.common.base.Objects toStringHelper(..)").isEmpty())
            problems.add("Objects.toStringHelper")
        if (!cu.findMethodCalls("com.google.common.collect.Iterators emptyIterator()").isEmpty())
            problems.add("Iterators.emptyIterator")
        if (!cu.findMethodCalls("com.google.common.util.concurrent.Futures get(..)").isEmpty())
            problems.add("Futures.get")
        if (!cu.findMethodCalls("com.google.common.util.concurrent.Futures withFallback(..)").isEmpty())
            problems.add("Futures.withFallback")
        if (!cu.findMethodCalls("com.google.common.util.concurrent.Futures transform(com.google.common.util.concurrent.ListenableFuture, com.google.common.util.concurrent.AsyncFunction, ..)").isEmpty())
            problems.add("Futures.transform")
        if (!cu.findMethodCalls("com.google.common.reflect.TypeToken isAssignableFrom(..)").isEmpty())
            problems.add("TypeToken.isAssignableFrom")
        if (!cu.findMethodCalls("com.google.common.util.concurrent.MoreExecutors sameThreadExecutor()").isEmpty())
            problems.add("MoreExecutors.sameThreadExecutor")

        return problems
    }

    fun guava(version: String): File {
        val jar = File(".rewrite/dependencies/guava-$version.jar")
        jar.parentFile.mkdirs()
        if(!jar.exists()) {
            print("Downloading guava $version to cache...")
            URL("http://repo1.maven.org/maven2/com/google/guava/guava/$version/guava-$version.jar").openStream().copyTo(jar.outputStream())
            println(" done!");
        }
        return jar
    }
}

data class JavaSourceRow(val repo: String, val path: String, val source: String)