package dgroomes.queryengine;

import dgroomes.queryengine.Executor.QueryResult;
import dgroomes.queryengine.Executor.QueryResult.Success;
import dgroomes.queryengine.ObjectGraph.Column.IntegerColumn;
import dgroomes.queryengine.Query.SingleFieldIntegerQuery;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static dgroomes.queryengine.ObjectGraph.*;
import static org.assertj.core.api.Assertions.assertThat;


/**
 * Let's write some test code to help drive our design of the API.... I'm starting from square one here.
 * <p>
 * Note: maybe consider creating a AssertJ extensions to make the test code more palatable. It's quite verbose. But also
 * consider if that's overkill.
 */
public class QueryEngineTest {

  /**
   * [Happy Path]
   * Single-field integer query over a direct array.
   * <p>
   */
  @Test
  void intQuery_intArray() {
    // Arrange
    //
    // Let's write a simple query over simple data.
    //
    // Data-under-test. This type is like a single-column table.
    IntegerColumn corpus = ofInts(-1, 0, 1, 2, 3);
    // Let's search for positive (non-zero) numbers.
    SingleFieldIntegerQuery query = integerUnderTest -> integerUnderTest > 0;

    // Act
    QueryResult result = Executor.match(query, corpus);

    // Assert
    assertThat(result).isInstanceOf(Success.class);
    Success success = (Success) result;
    Object matches = success.matches();
    assertThat(matches).isInstanceOf(int[].class);
    int[] intMatches = (int[]) matches;
    assertThat(intMatches).containsExactly(1, 2, 3);
  }

  /**
   * [Happy Path]
   * Ordinal integer query over a multi-field (i.e. column) type that contains one column.
   */
  @Test
  void intQuery_multiFieldType_oneColumn() {
    MultiColumnEntity corpus = ofColumns(ofInts(-1, 0, 1, 2, 3));
    var query = new Query.OrdinalSingleFieldIntegerQuery(0, i -> i > 0);

    // Act
    QueryResult result = Executor.match(query, corpus);

    // Assert
    assertThat(result).isInstanceOf(Success.class);
    Success success = (Success) result;
    Object matches = success.matches();
    assertThat(matches).isInstanceOf(MultiColumnEntity.class);
    MultiColumnEntity multiColumnEntityMatches = (MultiColumnEntity) matches;
    assertThat(multiColumnEntityMatches.columns()).hasSize(1);
    ObjectGraph.Column firstColumn = multiColumnEntityMatches.columns().get(0);
    assertThat(firstColumn).isInstanceOf(IntegerColumn.class);
    var intMatches = (IntegerColumn) firstColumn;
    assertThat(intMatches.ints()).containsExactly(1, 2, 3);
  }

  /**
   * [Happy Path]
   * Ordinal integer query over a multi-field (i.e. column) type that contains two columns.
   */
  @Test
  void intQuery_multiFieldType_twoColumns() {
    MultiColumnEntity corpus = ofColumns(
            // City names
            ofStrings("Minneapolis", "Rochester", "Duluth"),

            // City populations
            ofInts(425_336, 121_395, 86_697));
    var query = new Query.OrdinalSingleFieldIntegerQuery(1, pop -> pop > 100_000 && pop < 150_000);

    // Act
    QueryResult result = Executor.match(query, corpus);

    // Assert
    assertThat(result).isInstanceOf(Success.class);
    Success success = (Success) result;
    Object matches = success.matches();
    assertThat(matches).isInstanceOf(MultiColumnEntity.class);
    MultiColumnEntity multiColumnEntityMatches = (MultiColumnEntity) matches;
    assertThat(multiColumnEntityMatches.columns()).hasSize(2);
    ObjectGraph.Column cityColumn = multiColumnEntityMatches.columns().get(0);
    assertThat(cityColumn).isInstanceOf(Column.StringColumn.class);
    var cityMatches = (Column.StringColumn) cityColumn;
    assertThat(cityMatches.strings()).containsExactly("Rochester");
  }

  /**
   * [Happy Path]
   * <p>
   * Associations. Given a type X that is associated with another type Y, query for entities of X on a property of Y.
   * Specifically, let's model cities, states and the "contained in" association from city to state.
   */
  @Test
  @Disabled("not yet implemented")
  void queryOnAssociationProperty() {
    MultiColumnEntity cities = ofColumns(ofStrings("Minneapolis", "Pierre", "Duluth"));
    MultiColumnEntity states = ofColumns(ofStrings("Minnesota", "South Dakota"));
    // The "contained in" association from city to state. It is based on the index position of the cities and states
    // expressed above.
    cities.associateTo(states,
            toOne(0) /* Minneapolis is contained in Minnesota */,
            toOne(1) /* Pierre is contained in South Dakota */,
            toOne(0) /* Duluth is contained in Minnesota */);
    // todo I need to create a query type that can express this query.
    // Note: 1 is the ordinal position of the "contained in" association column to the state collection.
    // 0 is the ordinal position of the state name column in the state collection.
    var query = new Query.PointerSingleFieldStringQuery(new Query.Pointer.NestedPointer(1, new Query.Pointer.Ordinal(0)), stateNameUnderTest -> stateNameUnderTest.startsWith("South"));

    // Act
    QueryResult result = Executor.match(query, cities);

    // Assert
    assertThat(result).isInstanceOf(Success.class);
    Success success = (Success) result;
    Object matches = success.matches();
    assertThat(matches).isInstanceOf(MultiColumnEntity.class);
    MultiColumnEntity multiColumnEntityMatches = (MultiColumnEntity) matches;
    assertThat(multiColumnEntityMatches.columns()).hasSize(2);
    ObjectGraph.Column cityColumn = multiColumnEntityMatches.columns().get(0);
    assertThat(cityColumn).isInstanceOf(Column.StringColumn.class);
    var cityMatches = (Column.StringColumn) cityColumn;
    assertThat(cityMatches.strings()).containsExactly("Pierre");
  }
}
