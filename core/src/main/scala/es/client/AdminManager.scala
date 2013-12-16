/*
 *  Copyright (c) 2009-2013 - The Net Planet Europe S.R.L.  All Rights Reserved.
 */

package es.client

/**
 * Created by IntelliJ IDEA.
 * User: alberto
 * Date: 21/02/13
 * Time: 11:07
 */
case class AdminManager(client: Client) {
  lazy val indices = IndexManager(client)
  lazy val cluster = ClusterManager(client)
}
