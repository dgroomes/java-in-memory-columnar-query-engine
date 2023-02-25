package dgroomes;

import dgroomes.geography.GeographyGraph;
import dgroomes.loader.GeographiesLoader;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TheTest {

  @Test
  void placeholder() {
    assertThat(true).isTrue();
  }

  @Test
  void loadObjectGraph() {
    GeographyGraph graph = GeographiesLoader.loadFromFile();

    assertThat(graph.zips()).hasSize(29_353);
    assertThat(graph.zipToCity()).hasSize(29_353);
    assertThat(graph.cities()).hasSize(25_701);
    assertThat(graph.cityToState()).hasSize(25_701);
    assertThat(graph.states()).hasSize(51); // 50 states + DC
  }
}