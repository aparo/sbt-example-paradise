import sbt._

object Dependencies {
  val resolutionRepos = Seq(

    //default repos
    DefaultMavenRepository,

    // Sonatype repo
    Resolver.sonatypeRepo("releases"),
    Resolver.sonatypeRepo("snapshots"),

    //Resolver.url("Scala Tools Repository", url("http://scala-tools.org/repo-releases")),
    "twitter-repo" at "http://maven.twttr.com",
    // Spray repo
    "Spray Repository" at "http://repo.spray.io",

    // Typesafe repo
    "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe Snapshots Repository" at "http://repo.typesafe.com/typesafe/snapshots/",

    // Scala-tools.org substitute
    "SonaScalaTools" at "http://oss.sonatype.org/content/groups/scala-tools/",
    // For geocoder jar at http://jgeocoder.sourceforge.net/
    //,"Drexel" at "https://www.cs.drexel.edu/~zl25/maven2/repo"

    // Snapshots: the bleeding edge
    //,"snapshots-repo" at "http://www.scala-tools.org/repo-snapshots"
    "codehale repo" at "http://repo.codahale.com"
  )

  def listUnmanaged(base: RichFile): Keys.Classpath = {
    val baseDirectories = (base / "custom-libs")
    (baseDirectories ** "*.jar").classpath
  }

  def compile   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")
  def provided  (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")
  def test      (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")
  def runtime   (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")
  def container (deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

    lazy val scalaVersionFull="2.10.3"

  val jacksonCore   = "com.fasterxml.jackson.core" % "jackson-core" % "2.1.3"
  val jacksonDatabind = "com.fasterxml.jackson.core" % "jackson-databind" % "2.1.3"
  val jacksonJaxrs = "com.fasterxml.jackson.jaxrs" % "jackson-jaxrs-json-provider" % "2.1.3"
  val jacksonHibernate = "com.fasterxml.jackson.datatype" % "jackson-datatype-hibernate4" % "2.1.2"
  val jacksonScala       = "com.fasterxml.jackson.module"           %% "jackson-module-scala"         % "2.1.3"
  val akkaVersion                   = "2.2.3"
  val akkaActor                     = "com.typesafe.akka"                      %%  "akka-actor"                  % akkaVersion
  val akkaSlf4j                     = "com.typesafe.akka"                      %%  "akka-slf4j"                  % akkaVersion
  val akkaTestKit                   = "com.typesafe.akka"                      %%  "akka-testkit"                % akkaVersion

  // Joda 
  lazy val joda = "org.joda" % "joda-convert" % "1.2"
  lazy val jodaTime = "joda-time" % "joda-time" % "2.1"
  lazy val jodaComponents = Seq( joda, jodaTime )

  lazy val liftVersion       = "2.5.1"

  // liftweb
  lazy val liftJson = "net.liftweb"      %% "lift-json"          % liftVersion          % "compile"  
  lazy val liftJsonExt          =  "net.liftweb" %% "lift-json-ext" % liftVersion
  lazy val liftRecord        =  "net.liftweb" %% "lift-record" % liftVersion
  lazy val liftMongoDb       =  "net.liftweb" %% "lift-mongodb" % liftVersion

  lazy val liftMongoDBRecord = "net.liftweb" %% "lift-mongodb-record"          % liftVersion          % "compile"
  lazy val liftUtil = "net.liftweb" %% "lift-util"          % liftVersion          % "compile" 
  lazy val liftWebkit = "net.liftweb" %% "lift-webkit"          % liftVersion          % "compile"
  lazy val liftMapper = "net.liftweb" %% "lift-mapper"          % liftVersion          % "compile"
  lazy val liftQuerylRecord = "net.liftweb" %% "lift-squeryl-record"          % liftVersion          % "compile"
  //lazy val liftWizard = "net.liftweb" %% "lift-wizard"          % liftVersion          % "compile"
  lazy val liftTestKit = "net.liftweb" %% "lift-testkit"          % liftVersion          % "test"
  lazy val liftComponents = Seq( liftJson, liftJsonExt, liftMongoDBRecord, liftUtil, liftWebkit, liftMapper,
    liftQuerylRecord, liftTestKit) //liftWizard, 


  lazy val junit                    = "junit" % "junit-dep" % "4.11" % "test"
  lazy val scalatest = "org.scalatest" % "scalatest_2.10" % "2.0.M6-SNAP36" % "test"
  lazy val specs2 = "org.specs2" %% "specs2" % "2.3.6" % "test"
  lazy val mockito = "org.mockito" % "mockito-all" % "1.9.5" % "test"
  lazy val specs2Components = Seq( specs2, junit, scalatest )

//  lazy val common_io ="org.apache.commons" % "commons-io" % "1.3.2"
  lazy val common_io ="org.apache.directory.studio" % "org.apache.commons.io" % "2.4"
  lazy val commonIoComponents = Seq( common_io )

  lazy val htmlParser               = "org.htmlparser" % "htmlparser" % "2.1"

  lazy val parboiled = "org.parboiled" %% "parboiled-scala" % "1.1.6"

  //lazy val scalaReflect = "org.scala-lang" % "scala-reflect" % "2.11.0-M3"
//  lazy val scalaLang = "org.scala-lang" % "scala-library-all" % "2.10.0-RC2"
  //lazy val scalaReflect = "org.scala-lang.virtualized" % "scala-reflect" % "2.10.2-RC1"

  lazy val scalaReflect             = "org.scala-lang" % "scala-reflect" % "2.10.3"
  lazy val scalaReflectComponents = Seq(  scalaReflect )

  // Scalaz https://github.com/scalaz/scalaz
  // From http://repo1.maven.org/maven2/org/scalaz
  // Use import scalaz._; import Scalaz._
  lazy val scalaz = "org.scalaz" %% "scalaz-core" % "7.0.4"
  lazy val scalazComponents = Seq( scalaz )

  lazy val scalaIOFile = "com.github.scala-incubator.io" %% "scala-io-file" % "0.4.2"

  lazy val scala_compiler = "org.scala-lang" % "scala-compiler" % "2.10.3"


  lazy val logBack = "ch.qos.logback"     % "logback-classic" % "1.0.7"

  lazy val reactiveMongo = "org.reactivemongo" % "reactivemongo_2.10" % "0.9"

  lazy val logback_version = "1.0.13"
  lazy val logback_classic   = "ch.qos.logback" % "logback-classic" % logback_version
  lazy val logback_core      = "ch.qos.logback" % "logback-core" % logback_version
  lazy val logback_parent    = "ch.qos.logback" % "logback-parent" % logback_version
  lazy val logbackComponents = Seq(logback_classic,logback_core,logback_parent)

  lazy val twitterUtilVersion = "6.8.1"

  // Twitter-Util https://github.com/twitter/util
  lazy val twitterUtil = "com.twitter" % "util-core_2.10" % twitterUtilVersion
  lazy val twitterEval = "com.twitter" %% "util-eval" % twitterUtilVersion
  // Twitter-finagle https://github.com/twitter/finagle.git
  lazy val finagleVersion = "6.8.1"
  lazy val finagleHTTP = "com.twitter" %% "finagle-http" % finagleVersion
  lazy val finagleThrift = "com.twitter" %% "finagle-thrift" % finagleVersion
  lazy val ostrich = "com.twitter" %% "ostrich" % "9.1.2"


  lazy val twitterComponents = Seq( twitterUtil, finagleHTTP, ostrich, twitterEval)//finagleThrift,


  lazy val apacheHttpClient = "org.apache.httpcomponents" % "httpclient" % "4.3"
  lazy val apacheHttpMime   = "org.apache.httpcomponents" % "httpmime"   % "4.3"
  lazy val apacheHttpCore = "org.apache.httpcomponents" % "httpcomponents-core" % "4.3"
//  lazy val httpComponents = Seq( http_core, http_client )

  //lazy val scalaUri = "com.github.theon" %% "scala-uri" % "0.3.6"
  lazy val scalaUri = "com.github.theon" %% "scala-uri" % "0.3.7-SNAPSHOT"

  lazy val jcifsLib = "jcifs" % "jcifs" % "1.3.17"
  lazy val jcifsLibComponents = Seq( jcifsLib )

  lazy val jedisLib =   "redis.clients" % "jedis" % "2.1.0"
  lazy val jedisComponents = Seq(jedisLib)

  // Apache PDFBox http://pdfbox.apache.org/
  // From http://repo1.maven.org/maven2/org/apache/pdfbox/pdfbox/
//  lazy val pdfBox = "org.apache.pdfbox" % "pdfbox" % "1.7.1"
//  lazy val pdfBoxComponents = Seq( pdfBox )

  // H2DB
  // From http://repo1.maven.org/maven2/com/h2database/h2/
  lazy val h2DB = "com.h2database" % "h2" % "1.3.170" // % "provided"
  lazy val h2DBComponents = Seq( h2DB )


  // PostgreSQL 
  // From http://repo1.maven.org/maven2/postgresql/postgresql/
  lazy val posgresDB = "postgresql" % "postgresql" % "9.1-901.jdbc4" // % "provided" 
  lazy val posgresDBComponents = Seq( posgresDB )

  //Lib Thrift
  lazy val libThrift =  "org.apache.thrift" % "libthrift" % "0.8.0"
  lazy val libThriftComponets = Seq(libThrift)

  lazy val libTika ="org.apache.tika" % "tika-parsers" % "1.4-TNP"
  lazy val libTikaComponents = Seq(libTika)

  lazy val poiVersion = "3.10-beta2"
  lazy val libPoi ="org.apache.poi" % "poi" % poiVersion
  lazy val libPoiScratchpad ="org.apache.poi" % "poi-scratchpad" % poiVersion
  lazy val libPoiOOXML ="org.apache.poi" % "poi-ooxml" % poiVersion
  lazy val libPoiOOXMLSchema= "org.apache.poi" % "ooxml-schemas" % "1.0"
  lazy val libPoiComponents = Seq(libPoi, libPoiScratchpad, libPoiOOXML, libPoiOOXMLSchema)


  lazy val libCommonsVfs ="org.apache.commons" % "commons-vfs2" % "2.0"
  lazy val libCommonsVfsComponents = Seq (libCommonsVfs)

  lazy val jsoupLib = "org.jsoup" % "jsoup" % "1.7.2"
  lazy val jsoupComponets = Seq( jsoupLib )

  lazy val slf4jApi = "org.slf4j" % "slf4j-api" % "1.6.6"
  lazy val log4jOverSlf4j =  "org.slf4j" % "log4j-over-slf4j" % "1.6.6" % "test"

  lazy val log4jLib = "log4j" % "log4j" % "1.2.17" % "test"
  lazy val log4jLibProvided = "log4j" % "log4j" % "1.2.17" % "provided"
  lazy val log4jComponets = Seq( log4jLib )

  lazy val mongoDbBson = "org.mongodb" % "bson" % "2.10.1"
  lazy val mongoDbDriver = "org.mongodb" % "mongodb-java-driver" % "2.10.1"
//  lazy val mongoDbBson = "org.mongodb" % "bson" % "2.9.3"
//  lazy val mongoDbDriver = "org.mongodb" % "mongodb-java-driver" % "2.9.3"

  lazy val mongoDbComponents = Seq(mongoDbBson , mongoDbDriver )

  // https://code.google.com/p/scalascriptengine/
  lazy val scalaEngineLib ="com.googlecode.scalascriptengine" % "scalascriptengine" % "1.3.7-2.10.3"
  lazy val scalaEngineComponents = Seq(scalaEngineLib, scala_compiler) //it depends from scala compiler

  lazy val crawler4jLib =   "edu.uci.ics" % "crawler4j" % "3.5"
  lazy val crawler4jComponents = Seq(crawler4jLib)

  lazy val echidnasearchCore = "net.thenetplanet" % "echidnasearch" % "1.4.7" % "1.4.7-SNAPSHOT"
  lazy val echidnasearchComponents = Seq ( echidnasearchCore) //, echidnasearchRecord, echidnasearchCore

  lazy val json4sVersion = "3.2.5"
  lazy val json4sNative = "org.json4s" %% "json4s-native" % json4sVersion
  lazy val json4sExt = "org.json4s" %% "json4s-ext" % json4sVersion
  lazy val json4sNativeLift = "org.json4s" %% "json4s-native-lift" % json4sVersion
  lazy val json4sScalaz = "org.json4s" %% "json4s-scalaz" % json4sVersion

  lazy val pdfBox = "org.apache.pdfbox" % "pdfbox" % "1.8.1"
  lazy val pdfBoxComponent = Seq(pdfBox)

  lazy val shapeless = "com.chuusai" %% "shapeless" % "1.2.4"
  lazy val shapelessComponents = Seq(shapeless)

  lazy val userAgentUtils= "nl.bitwalker" % "UserAgentUtils" % "1.2.4"
  // Others
  //lazy val servlet_api  = "javax.servlet" % "servlet-api" % "2.5"
  lazy val jetty        = "org.eclipse.jetty" % "jetty-webapp" % "8.1.7.v20120910"  % "container,test"
  lazy val jettyOrbit        =  "org.eclipse.jetty.orbit" % "javax.servlet" % "3.0.0.v201112011016" % "container,compile" artifacts Artifact("javax.servlet", "jar", "jar")
  lazy val jolbox        = "com.jolbox"               % "bonecp"         % "0.7.1.RELEASE"    % "compile->default"
  // lazy val jopt         = "net.sf.jopt-simple" % "jopt-simple" % "4.4-beta-3" withSources()
  lazy val opencsv      = "net.sf.opencsv" % "opencsv" % "2.3" // from Sonatype
  lazy val logback      = "ch.qos.logback" % "logback-classic" % "1.0.3" % "provided"
  // lazy val bouncycastle = "org.bouncycastle"  % "bcprov-jdk16" % "1.46"  withSources,
  // lazy val junitInterf  = "com.novocode" % "junit-interface" % "0.8" % "test->default"
  // lazy val jgeocoder    = "net.sourceforge.jgeocoder" % "jgeocoder" % "0.4.1" withSources // from Drexel
  // lazy val poi          = "org.apache.poi" % "poi" % "3.8" withSources
  // lazy val apacheIO     = "commons-io" % "commons-io" % "2.4" // org.apache.commons http://commons.apache.org/io/
  // lazy val specs        = "org.scala-tools.testing" %% "specs" % "1.6.5-SNAPSHOT" % "test" withSources
  lazy val scalacheck   = "org.scalacheck" %% "scalacheck" % "1.10.1" % "test"
  //lazy val scalatest = "org.scalatest" % "scalatest_2.10" % "2.0.M6-SNAP8" % "test"
  lazy val jbcript = "org.mindrot" % "jbcrypt" % "0.3m" % "compile"
  lazy val extraComponents = Seq( jolbox, logback, scalatest )

  lazy val webserverComponents = Seq(jetty, jettyOrbit)


  lazy val nxparser = "org.semanticweb.yars" % "nxparser" % "1.2.2" % "compile"
  lazy val jewelcli = "com.lexicalscope.jewelcli" % "jewelcli" % "0.8.6" % "compile"

  lazy val jython = "org.python" % "jython-standalone" % "2.7a" % "compile"
  lazy val rhino = "org.mozilla" % "rhino" % "1.7R4" % "compile"

  lazy val guava = "com.google.guava" % "guava" % "15.0" % "compile"

  lazy val hamcrest = "org.hamcrest" % "hamcrest-all" % "1.3" % "test"
  lazy val testng = "org.testng" % "testng" % "6.3.1" % "test"

  lazy val oauthCore = "com.nulab-inc" %% "scala-oauth2-core" % "0.2.1"
  lazy val newman = "com.stackmob" %% "newman" % "1.3.2"

  // Commnd line parsing https://github.com/scopt/scopt
  lazy val scopt="com.github.scopt" %% "scopt" % "3.2.0"

}