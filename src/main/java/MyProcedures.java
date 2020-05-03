import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MyProcedures {
    public static void main(String[] args) {
        MyProcedures.tryPredicate();
        MyProcedures.tryConsumer();
        MyProcedures.tryFunction();
        MyProcedures.tryMethodReference();
        MyProcedures.tryComposingComparator();
        MyProcedures.tryComposingPredicate();
    }

    public static void printTestResult(
            String javaFeatureName,  String taskDescription, Boolean condition
    ){
        System.out.println(String.format(
                "%s: Using feature \"%s\", do \"%s\"",
                condition ? "PASS" : "FAIL",
                javaFeatureName,
                taskDescription
        ));
    }

    public static void tryPredicate(){
        // Apparently IntPredicate is necessary for working with IntStream
        // because of differences between int and Integer.
        IntPredicate isOdd = x -> (x & 1) == 1;
        int[] oddInts = IntStream.rangeClosed(1, 10).filter(isOdd).toArray();

        MyProcedures.printTestResult(
                "Predicate",
                "filter array for odd values",
                Arrays.equals(oddInts, new int[]{1, 3, 5, 7, 9})
        );
    }

    public static void tryConsumer(){
        Map<String, Boolean> housemates = new ConcurrentHashMap<>(Map.of(
                "Jose", false, "Tran", false,
                "Ahmed", false, "Ashish", false
        ));

        // A consumer which sets all entries in the hash-map to true.
        Consumer<Map.Entry<String, Boolean>> setToTrue =
                entry -> housemates.put(entry.getKey(), true);

        housemates.entrySet().parallelStream().forEach(setToTrue);

        MyProcedures.printTestResult(
                "Consumer",
                "set all entries to true",
                housemates.values().parallelStream().allMatch(Boolean::valueOf)
        );
    }

    public static void tryFunction(){
        // We could've directly passed this Function, but let's note the type.
        Function<Integer, String> myIntToString = Object::toString;

        // A List of ints parsed into strings.
        List<String> parsedInts = Stream.of(1, 2, 3)
                .map(myIntToString)
                .collect(Collectors.toList());

        MyProcedures.printTestResult(
                "Function",
                "convert Integers to Strings",
                parsedInts.equals(List.of("1", "2", "3"))
        );

        // Primitive Specialization is necessary to avoid type casting.
        // We could've directly passed this Function, but let's note the type.
        ToIntFunction<String> toLen = String::length;

        // An array of the lengths of the housemates' names.
        int[] housemateLengths = Stream.of("Jose", "Tran", "Ahmed", "Ashish")
                .mapToInt(toLen)
                .toArray();

        MyProcedures.printTestResult(
                "Function",
                "map names of housemates to length of names",
                Arrays.equals(housemateLengths, new int[]{4, 4, 5, 6})
        );
    }

    public static void tryMethodReference(){
        int[] parsedInts = Stream.of("1", "2", "3")
                .mapToInt(Integer::parseInt)
                .toArray();

        MyProcedures.printTestResult(
                "Static Method Reference",
                "convert Strings to ints",
                Arrays.equals(parsedInts, new int[]{1, 2, 3})
        );

        List<String> intStrings = IntStream.of(1, 2, 3)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());

        MyProcedures.printTestResult(
                "Instance Method Reference",
                "convert ints to Strings",
                intStrings.equals(List.of("1", "2", "3"))
        );

        class MyInt {
            final int x;

            public MyInt(int x) {
                this.x = x;
            }

            public boolean equals(MyInt other) {
                return this.x == other.x;
            }

            public long countInstances(List<MyInt> list){
                return list.stream().filter(this::equals).count();
            }
        }

        MyProcedures.printTestResult(
                "Bound Method Reference",
                "count the number of instances in a list",
                2 == new MyInt(0).countInstances(
                        Stream.of(0, 1, 0, 2).map(MyInt::new)
                                .collect(Collectors.toList())
                )
        );
    }

    public static void tryComposingComparator(){
        int[][] coordinates = new int[][]{
                {0, 0}, {1, 0}, {0, 1}
        };

        // Sort arrays in descending order:
        // by highest y-value, then by lowest x-value.
        Arrays.sort(
                coordinates,
                Comparator
                        // sort descending by y-value
                        .comparing((int[] xy) -> xy[1])
                        // then descending by lower x-value
                        .thenComparing((int[] xy) -> -xy[0])
                        // Note: Java sorts in ascending order, so reverse into
                        // descending order.
                        .reversed()
        );

        MyProcedures.printTestResult(
                "Comparator Composing",
                "sort a list of coordinates by y-value"
                        +", then by lower x-value",
                Arrays.deepEquals(coordinates,
                        new int[][]{{0, 1}, {0, 0}, {1, 0}})
        );
    }

    public static void tryComposingPredicate(){
        int[] posMultiplesOf3LessThan10 = IntStream.rangeClosed(-10, 10).filter(
                ((IntPredicate) x -> x > 0)
                        .and(x -> (x % 3) == 0)
                        .and(x -> x < 10)
        ).toArray();

        MyProcedures.printTestResult(
                "Predicate Composing",
                "filter for positive ints",
                Arrays.equals(posMultiplesOf3LessThan10, new int[]{3, 6, 9})
        );
    }
}
