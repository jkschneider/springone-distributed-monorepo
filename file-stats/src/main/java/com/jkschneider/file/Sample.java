package com.jkschneider.file;

import org.zeroturnaround.zip.ZipUtil;

import java.io.File;

public class Sample {
  void foo(File f) {
    ZipUtil.unpack(f, f);
  }
}
