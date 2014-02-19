/*
Copyright 2014 Twitter, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package com.twitter.hraven;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * tests the {@link HdfsStatsKeyConverter} class
 */
public class TestQualifiedPathKey {

  private static final String cluster1 = "cluster1";
  private static final String path1 = "path1";

  @Test
  public void testConstructor1() throws Exception {

    QualifiedPathKey key1 = new QualifiedPathKey(cluster1, path1);
    testKeyComponents(key1);
  }

  @Test
  public void testEquality() throws Exception {
    QualifiedPathKey key1 = new QualifiedPathKey(cluster1, path1);
    QualifiedPathKey key2 = new QualifiedPathKey(cluster1, path1);
    assertEquals(key1.hashCode(), key2.hashCode());
    assertEquals(key1.compareTo(key2), 0);
    assertEquals(key1, key2);
  }

  private void testKeyComponents( QualifiedPathKey key1) {
    assertNotNull(key1);
    assertEquals(key1.getCluster(), cluster1);
    assertEquals(key1.getPath(), path1);
  }

}
