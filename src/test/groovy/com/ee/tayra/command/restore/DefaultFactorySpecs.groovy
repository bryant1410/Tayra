package com.ee.tayra.command.restore

import static com.ee.tayra.ConnectionFactory.*
import spock.lang.Specification

import com.ee.tayra.io.listener.RestoreProgressReporter
import com.ee.tayra.io.writer.OplogReplayer
import com.ee.tayra.io.writer.SelectiveOplogReplayer
import com.mongodb.MongoClient

class DefaultFactorySpecs extends Specification {

  private RestoreCmdDefaults config
  private def factory
  private MongoClient ignoreMongo
  private PrintWriter ignoreConsole

  def setup() {
    config = new RestoreCmdDefaults()
    config.destination = secureSrcNode
    config.port = secureSrcPort
    config.username = username
    config.password = password
    config.exceptionFile = 'exception.documents'
    factory =  new DefaultFactory(config, ignoreMongo, ignoreConsole)
  }

  def createsEmptyListener() {
    expect: 'listener created is instance of EmptyProgressReporter'
      factory.createListener().getClass() == RestoreProgressReporter
  }

  def createsEmptyReporter() {
    expect: 'reporter created is instance of EmptyProgressReporter'
      factory.createReporter().getClass() == RestoreProgressReporter
  }

  def createsWriters() {
    given:
      config.sNs = namespace
      config.sUntil = untilDate
      factory =  new DefaultFactory(config, ignoreMongo, ignoreConsole)

    expect: 'writer created is type of klass'
      factory.createWriter().getClass() == klass

    where: 'namespace and until are varied as...'
      namespace |     untilDate        | klass
          ''    |         ''           | OplogReplayer
        'test'  |         ''           | SelectiveOplogReplayer
          ''    |'2013-03-16T15:19:40Z'| SelectiveOplogReplayer
        'test'  |'2013-03-16T15:19:40Z'| SelectiveOplogReplayer
  }
}
