package com.jkschneider.codesearch.codesearch;

import java.io.PrintWriter;
import java.util.*;

import static java.util.Collections.*;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

class CallGraph {
  private final Map<Call, Set<Call>> reverseCallGraph = new HashMap<>();

  public void addCall(Call caller, Call callee) {
    reverseCallGraph.computeIfAbsent(callee, k -> new HashSet<>()).add(caller);
  }

  public Collection<Call> findCalls(String className, String methodName, String... args) {
    if (args.length > 0 && args[args.length - 1].equals("..")) {
      return reverseCallGraph.keySet().stream()
        .filter(call -> {
          if (!call.getOwner().equals(className) || !call.getName().equals(methodName) ||
            call.getArguments().length < args.length - 1) {
            return false;
          }

          for (int i = 0; i < args.length - 1; i++) {
            if (!args[i].equals(call.getArguments()[i])) {
              return false;
            }
          }
          return true;
        })
        .collect(toList());
    }

    Call call = new Call(className, methodName, args);
    if (reverseCallGraph.containsKey(call)) {
      return singletonList(call);
    }
    return emptyList();
  }

  /**
   * @return A collection of call chains leading to this method.
   */
  public Collection<List<Call>> callers(String className, String methodName, String... args) {
    Collection<Call> roots = findCalls(className, methodName, args);
    return callChain(roots.stream()
      .map(Collections::singletonList).collect(toList()));
  }

  private Collection<List<Call>> callChain(Collection<List<Call>> callChains) {
    return callChains.stream()
      .flatMap(chain -> reverseCallGraph.getOrDefault(chain.get(chain.size() - 1), emptySet())
        .stream()
        .map(next -> {
          List<Call> extendedChain = new ArrayList<>(chain);
          Collections.addAll(extendedChain, next);
          return extendedChain;
        }))
      .collect(toList());
  }

//  public void print(Call call) {
//    print(call, 0);
//  }
//
//  private void print(Call call, int depth) {
//    if (depth > 50)
//      return;
//    System.out.println(StringUtils.repeat(" ", depth) + call.toString());
//    reverseCallGraph.getOrDefault(call, emptySet()).forEach(caller -> print(caller, depth + 1));
//  }

  public void printCalledMethodsSummary(PrintWriter pw) {
    reverseCallGraph.forEach((callee, callers) -> pw.print(callee.getOwner() + "," + callee.getName() + "," +
      Arrays.stream(callee.getArguments()).collect(joining(";")) + "," + callers.size() + "\n"));
  }
}