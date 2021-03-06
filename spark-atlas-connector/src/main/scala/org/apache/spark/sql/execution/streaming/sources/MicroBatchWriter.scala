/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.spark.sql.execution.streaming.sources

import org.apache.spark.sql.Row
import org.apache.spark.sql.catalyst.InternalRow
import org.apache.spark.sql.sources.v2.writer.{DataSourceWriter, DataWriterFactory, SupportsWriteInternalRow, WriterCommitMessage}
import org.apache.spark.sql.sources.v2.writer.streaming.StreamWriter

  /**
    * A [[DataSourceWriter]] used to hook V2 stream writers into a microbatch plan. It implements
    * the non-streaming interface, forwarding the batch ID determined at construction to a wrapped
    * streaming writer.
    */
class MicroBatchWriter(batchId: Long, writer: StreamWriter) extends DataSourceWriter {
  override def commit(messages: Array[WriterCommitMessage]): Unit = {
    writer.commit(batchId, messages)
  }

  override def abort(messages: Array[WriterCommitMessage]): Unit = writer.abort(batchId, messages)

  override def createWriterFactory(): DataWriterFactory[Row] = writer.createWriterFactory()
}

class InternalRowMicroBatchWriter(batchId: Long, val writer: StreamWriter)
  extends DataSourceWriter with SupportsWriteInternalRow {
  override def commit(messages: Array[WriterCommitMessage]): Unit = {
    writer.commit(batchId, messages)
  }

  override def abort(messages: Array[WriterCommitMessage]): Unit = writer.abort(batchId, messages)

  override def createInternalRowWriterFactory(): DataWriterFactory[InternalRow] =
    writer match {
      case w: SupportsWriteInternalRow => w.createInternalRowWriterFactory()
      case _ => throw new IllegalStateException(
        "InternalRowMicroBatchWriter should only be created with base writer support")
    }
}
