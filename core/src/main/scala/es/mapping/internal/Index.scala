package es.mapping.internal

import es.mapping.DocumentObjectField

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 05/03/13
 * Time: 17:53
 */
case class Index(name: String, documentTypes: Map[String, DocumentObjectField] = Map.empty[String, DocumentObjectField])
