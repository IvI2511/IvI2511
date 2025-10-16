import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AlgoritmosDeBusqueda {

    public static void main(String[] args) {
        List<SearchAlgorithm> algorithms = List.of(
            new LinearSearch(),
            new BinarySearch(),
            new InterpolationSearch()
        );

        List<Dataset> datasets = buildDatasets();
        List<TestOutcome> outcomes = new ArrayList<>();

        for (Dataset dataset : datasets) {
            System.out.println("==============================");
            System.out.println("Conjunto: " + dataset.name);
            System.out.println("Datos: " + formatArray(dataset.data));
            if (dataset.presentTarget != null) {
                System.out.println("Objetivo presente: " + dataset.presentTarget);
            }
            System.out.println("Objetivo ausente: " + dataset.missingTarget);

            for (SearchAlgorithm algorithm : algorithms) {
                if (dataset.presentTarget != null) {
                    TestOutcome foundOutcome = executeTest(algorithm, dataset, dataset.presentTarget, true);
                    outcomes.add(foundOutcome);
                    System.out.println(formatOutcome(foundOutcome));
                }
                TestOutcome missingOutcome = executeTest(algorithm, dataset, dataset.missingTarget, false);
                outcomes.add(missingOutcome);
                System.out.println(formatOutcome(missingOutcome));
            }
        }

        System.out.println("==============================");
        System.out.println("RESUMEN GLOBAL");
        Map<String, StatsAccumulator> resumen = buildSummary(outcomes);
        for (Map.Entry<String, StatsAccumulator> entry : resumen.entrySet()) {
            StatsAccumulator stats = entry.getValue();
            System.out.printf(
                "\nAlgoritmo: %s\n" +
                "  Pruebas válidas: %d (hallazgos: %d, fallos: %d)\n" +
                "  Pasos promedio (global): %.2f\n" +
                "  Tiempo promedio (global): %.4f ms\n",
                entry.getKey(),
                stats.validRuns,
                stats.foundRuns,
                stats.validRuns - stats.foundRuns,
                stats.validRuns > 0 ? (stats.totalSteps / (double) stats.validRuns) : 0.0,
                stats.validRuns > 0 ? (stats.totalTimeMs / stats.validRuns) : 0.0
            );
            if (stats.foundRuns > 0) {
                System.out.printf(
                    "  Pasos promedio cuando encuentra: %.2f\n  Tiempo promedio cuando encuentra: %.4f ms\n",
                    stats.stepsFound / (double) stats.foundRuns,
                    stats.timeFound / stats.foundRuns
                );
            }
            int misses = stats.validRuns - stats.foundRuns;
            if (misses > 0) {
                System.out.printf(
                    "  Pasos promedio cuando no encuentra: %.2f\n  Tiempo promedio cuando no encuentra: %.4f ms\n",
                    stats.stepsNotFound / (double) misses,
                    stats.timeNotFound / misses
                );
            }
            if (stats.invalidRuns > 0) {
                System.out.printf("  Casos no evaluados por datos inválidos: %d\n", stats.invalidRuns);
            }
        }
    }

    private static TestOutcome executeTest(SearchAlgorithm algorithm, Dataset dataset, int target, boolean expectedPresent) {
        SearchResult result = algorithm.search(dataset.data, target);
        return new TestOutcome(dataset, algorithm.name(), target, expectedPresent, result);
    }

    private static String formatOutcome(TestOutcome outcome) {
        StringBuilder sb = new StringBuilder();
        sb.append("  ").append(outcome.algorithmName).append(" -> ");
        sb.append(outcome.result.describe(outcome.target));
        if (outcome.expectedPresent != outcome.result.found && outcome.result.valid) {
            sb.append(" (resultado inesperado)");
        }
        if (!outcome.result.valid) {
            sb.append(" (datos no válidos)");
        }
        return sb.toString();
    }

    private static Map<String, StatsAccumulator> buildSummary(List<TestOutcome> outcomes) {
        Map<String, StatsAccumulator> resumen = new LinkedHashMap<>();
        for (TestOutcome outcome : outcomes) {
            StatsAccumulator stats = resumen.computeIfAbsent(outcome.algorithmName, k -> new StatsAccumulator());
            if (!outcome.result.valid) {
                stats.invalidRuns++;
                continue;
            }
            stats.validRuns++;
            stats.totalSteps += outcome.result.steps;
            stats.totalTimeMs += outcome.result.milliseconds;
            if (outcome.result.found) {
                stats.foundRuns++;
                stats.stepsFound += outcome.result.steps;
                stats.timeFound += outcome.result.milliseconds;
            } else {
                stats.stepsNotFound += outcome.result.steps;
                stats.timeNotFound += outcome.result.milliseconds;
            }
        }
        return resumen;
    }

    private static List<Dataset> buildDatasets() {
        List<Dataset> datasets = new ArrayList<>();
        datasets.add(new Dataset("Tamaño 1 - Nulo", null, null, 5));
        datasets.add(new Dataset("Tamaño 1 - Valor 0", new int[]{0}, 0, 1));
        datasets.add(new Dataset("Tamaño 1 - Número aleatorio", new int[]{7}, 7, 2));

        datasets.add(new Dataset("Tamaño 3 - Nulo", null, null, 6));
        datasets.add(new Dataset("Tamaño 3 - Aleatorio uniforme", new int[]{3, 6, 9}, 6, 8));
        datasets.add(new Dataset("Tamaño 3 - Aleatorio no uniforme", new int[]{2, 2, 9}, 2, 8));
        datasets.add(new Dataset("Tamaño 3 - Ordenado uniforme", new int[]{1, 2, 3}, 2, 5));
        datasets.add(new Dataset("Tamaño 3 - Ordenado no uniforme", new int[]{1, 2, 5}, 5, 4));
        datasets.add(new Dataset("Tamaño 3 - Todos iguales", new int[]{4, 4, 4}, 4, 6));
        datasets.add(new Dataset("Tamaño 3 - Todos ceros", new int[]{0, 0, 0}, 0, 3));

        datasets.add(new Dataset("Tamaño 10 - Nulo", null, null, 10));
        datasets.add(new Dataset("Tamaño 10 - Aleatorio uniforme", new int[]{3, 17, 25, 41, 55, 62, 78, 80, 94, 99}, 62, 50));
        datasets.add(new Dataset("Tamaño 10 - Aleatorio no uniforme", new int[]{1, 2, 2, 3, 10, 50, 51, 90, 95, 100}, 50, 4));
        datasets.add(new Dataset("Tamaño 10 - Ordenado uniforme", new int[]{2, 4, 6, 8, 10, 12, 14, 16, 18, 20}, 14, 15));
        datasets.add(new Dataset("Tamaño 10 - Ordenado no uniforme", new int[]{1, 2, 4, 8, 16, 32, 40, 60, 70, 100}, 32, 33));
        datasets.add(new Dataset("Tamaño 10 - Todos iguales", new int[]{7, 7, 7, 7, 7, 7, 7, 7, 7, 7}, 7, 1));
        datasets.add(new Dataset("Tamaño 10 - Todos ceros", new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 0, 5));

        datasets.add(new Dataset("Tamaño 20 - Nulo", null, null, 20));
        datasets.add(new Dataset("Tamaño 20 - Aleatorio uniforme", new int[]{4, 7, 13, 15, 22, 25, 33, 36, 44, 49, 55, 59, 63, 70, 73, 77, 84, 89, 95, 99}, 55, 27));
        datasets.add(new Dataset("Tamaño 20 - Aleatorio no uniforme", new int[]{1, 1, 2, 5, 6, 15, 30, 30, 31, 80, 81, 82, 83, 150, 151, 152, 153, 300, 500, 1000}, 152, 999));
        datasets.add(new Dataset("Tamaño 20 - Ordenado uniforme", new int[]{0, 5, 10, 15, 20, 25, 30, 35, 40, 45, 50, 55, 60, 65, 70, 75, 80, 85, 90, 95}, 65, 42));
        datasets.add(new Dataset("Tamaño 20 - Ordenado no uniforme", new int[]{1, 2, 4, 8, 12, 16, 20, 30, 42, 50, 60, 65, 70, 72, 80, 82, 85, 90, 92, 100}, 72, 71));
        datasets.add(new Dataset("Tamaño 20 - Todos iguales", new int[]{9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9, 9}, 9, 8));
        datasets.add(new Dataset("Tamaño 20 - Todos ceros", new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0}, 0, 3));

        return datasets;
    }

    private static String formatArray(int[] array) {
        return array == null ? "null" : Arrays.toString(array);
    }

    private interface SearchAlgorithm {
        String name();
        SearchResult search(int[] array, int target);
    }

    private static class LinearSearch implements SearchAlgorithm {
        @Override
        public String name() {
            return "Búsqueda lineal";
        }

        @Override
        public SearchResult search(int[] array, int target) {
            long start = System.nanoTime();
            int steps = 0;
            if (array == null || array.length == 0) {
                double ms = elapsedMs(start);
                return SearchResult.invalid("La lista está vacía", steps, ms);
            }

            for (int i = 0; i < array.length; i++) {
                steps++;
                if (array[i] == target) {
                    double ms = elapsedMs(start);
                    return SearchResult.found(i, steps, ms);
                }
            }

            double ms = elapsedMs(start);
            return SearchResult.notFound(steps, ms);
        }
    }

    private static class BinarySearch implements SearchAlgorithm {
        @Override
        public String name() {
            return "Búsqueda binaria";
        }

        @Override
        public SearchResult search(int[] array, int target) {
            long start = System.nanoTime();
            int steps = 0;
            if (array == null || array.length == 0) {
                double ms = elapsedMs(start);
                return SearchResult.invalid("La lista está vacía", steps, ms);
            }

            int low = 0;
            int high = array.length - 1;

            while (low <= high) {
                int mid = low + (high - low) / 2;
                steps++;
                if (array[mid] == target) {
                    double ms = elapsedMs(start);
                    return SearchResult.found(mid, steps, ms);
                }
                if (array[mid] < target) {
                    low = mid + 1;
                } else {
                    high = mid - 1;
                }
            }

            double ms = elapsedMs(start);
            return SearchResult.notFound(steps, ms);
        }
    }

    private static class InterpolationSearch implements SearchAlgorithm {
        @Override
        public String name() {
            return "Búsqueda por interpolación";
        }

        @Override
        public SearchResult search(int[] array, int target) {
            long start = System.nanoTime();
            int steps = 0;
            if (array == null || array.length == 0) {
                double ms = elapsedMs(start);
                return SearchResult.invalid("La lista está vacía", steps, ms);
            }

            int low = 0;
            int high = array.length - 1;

            while (low <= high && target >= array[low] && target <= array[high]) {
                steps++;
                if (array[high] == array[low]) {
                    if (array[low] == target) {
                        double ms = elapsedMs(start);
                        return SearchResult.found(low, steps, ms);
                    }
                    break;
                }

                int pos = low + (int) ((long) (target - array[low]) * (high - low) / (array[high] - array[low]));

                if (pos < low || pos > high) {
                    break;
                }

                if (array[pos] == target) {
                    double ms = elapsedMs(start);
                    return SearchResult.found(pos, steps, ms);
                }

                if (array[pos] < target) {
                    low = pos + 1;
                } else {
                    high = pos - 1;
                }
            }

            double ms = elapsedMs(start);
            if (low < array.length && target < array[low]) {
                steps++;
            } else if (high >= 0 && target > array[high]) {
                steps++;
            }
            return SearchResult.notFound(steps, ms);
        }
    }

    private static double elapsedMs(long start) {
        long end = System.nanoTime();
        return (end - start) / 1_000_000.0;
    }

    private static class SearchResult {
        final boolean valid;
        final boolean found;
        final int index;
        final int steps;
        final double milliseconds;
        final String message;

        private SearchResult(boolean valid, boolean found, int index, int steps, double milliseconds, String message) {
            this.valid = valid;
            this.found = found;
            this.index = index;
            this.steps = steps;
            this.milliseconds = milliseconds;
            this.message = message;
        }

        static SearchResult found(int index, int steps, double ms) {
            return new SearchResult(true, true, index, steps, ms, "Se ha encontrado el elemento en la posición " + index);
        }

        static SearchResult notFound(int steps, double ms) {
            return new SearchResult(true, false, -1, steps, ms, "No se ha encontrado el elemento");
        }

        static SearchResult invalid(String message, int steps, double ms) {
            return new SearchResult(false, false, -1, steps, ms, message);
        }

        String describe(int target) {
            StringBuilder sb = new StringBuilder(message);
            sb.append(" | objetivo: ").append(target);
            sb.append(" | pasos: ").append(steps);
            sb.append(String.format(" | tiempo: %.4f ms", milliseconds));
            if (valid && found) {
                sb.append(" | índice: ").append(index);
            }
            return sb.toString();
        }
    }

    private static class Dataset {
        final String name;
        final int[] data;
        final Integer presentTarget;
        final int missingTarget;

        Dataset(String name, int[] data, Integer presentTarget, int missingTarget) {
            this.name = name;
            this.data = data;
            this.presentTarget = presentTarget;
            this.missingTarget = missingTarget;
        }
    }

    private static class TestOutcome {
        final Dataset dataset;
        final String algorithmName;
        final int target;
        final boolean expectedPresent;
        final SearchResult result;

        TestOutcome(Dataset dataset, String algorithmName, int target, boolean expectedPresent, SearchResult result) {
            this.dataset = dataset;
            this.algorithmName = algorithmName;
            this.target = target;
            this.expectedPresent = expectedPresent;
            this.result = result;
        }
    }

    private static class StatsAccumulator {
        int validRuns;
        int foundRuns;
        int invalidRuns;
        long totalSteps;
        double totalTimeMs;
        long stepsFound;
        double timeFound;
        long stepsNotFound;
        double timeNotFound;
    }
}
