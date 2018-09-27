package com.jkschneider.codesearch.codesearch;

import lombok.Data;

import java.util.Arrays;

import static java.util.stream.Collectors.joining;

@Data
class Call {
  final String owner;
  final String name;
  final String[] arguments;

  public String toString() {
    return owner + "#" + name + "(" + Arrays.stream(arguments).collect(joining(", ")) + ")";
  }
}