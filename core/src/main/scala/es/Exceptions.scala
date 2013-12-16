package es.exceptions

class ESException(msg: String, status: Int = 0) extends RuntimeException {}


class NoServerAvailable(msg: String, status: Int) extends ESException(msg, status) {}

class NotFound(msg: String="Not Found", status: Int=404) extends ESException(msg, status) {}

class InvalidQuery(msg: String, status: Int) extends ESException(msg, status) {}


class InvalidParameterQuery(msg: String, status: Int) extends InvalidQuery(msg, status) {}


class QueryError(msg: String, status: Int) extends ESException(msg, status) {}


class QueryParameterError(msg: String, status: Int) extends ESException(msg, status) {}


class ScriptFieldsError(msg: String, status: Int) extends ESException(msg, status) {}


class InvalidParameter(msg: String, status: Int) extends ESException(msg, status) {}


class MissingValueException(msg: String, status: Int) extends ESException(msg, status) {}


class NotUniqueValueException(msg: String, status: Int) extends ESException(msg, status) {}


class ESIllegalArgumentException(msg: String, status: Int) extends ESException(msg, status) {}


class IndexMissingException(msg: String, status: Int) extends ESException(msg, status) {}


class NotFoundException(msg: String, status: Int) extends ESException(msg, status) {}


class AlreadyExistsException(msg: String, status: Int) extends ESException(msg, status) {}


class IndexAlreadyExistsException(msg: String, status: Int) extends AlreadyExistsException(msg, status)


class SearchPhaseExecutionException(msg: String, status: Int) extends ESException(msg, status) {}


class ReplicationShardOperationFailedException(msg: String, status: Int) extends ESException(msg, status) {}


class ClusterBlockException(msg: String, status: Int) extends ESException(msg, status) {}


class MapperParsingException(msg: String, status: Int) extends ESException(msg, status) {}


class ReduceSearchPhaseException(msg: String, status: Int) extends ESException(msg, status) {}


class VersionConflictEngineException(msg: String, status: Int) extends ESException(msg, status) {}


class DocumentAlreadyExistsException(msg: String, status: Int) extends ESException(msg, status) {}


class DocumentAlreadyExistsEngineException(msg: String, status: Int) extends ESException(msg, status) {}


class TypeMissingException(msg: String, status: Int) extends ESException(msg, status) {}


//mappings

class MappedFieldNotFoundException(msg: String, status: Int) extends ESException(msg, status) {}

