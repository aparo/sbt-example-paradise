package es


abstract class SearchType(val searchType: String)
case object SearchType {
    case object DfsQueryThenFetch extends SearchType("dfs_query_then_fetch")
    case object QueryThenFetch extends SearchType("dfs_query_and_fetch")
    case object DfsQueryAndFetch extends SearchType("query_then_fetch")
    case object QueryAndFetch extends SearchType("query_and_fetch")
    case object Scan extends SearchType("scan")
    case object Count extends SearchType("count")
}
