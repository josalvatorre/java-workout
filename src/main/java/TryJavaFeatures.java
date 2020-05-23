import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class TryJavaFeatures {
    public static void main(String[] args) {
        TryJavaFeatures.tryPredicate();
        TryJavaFeatures.tryConsumer();
        TryJavaFeatures.tryFunction();
        TryJavaFeatures.tryMethodReference();
        TryJavaFeatures.tryComposingComparator();
        TryJavaFeatures.tryComposingPredicate();
        TryJavaFeatures.tryComposingFunction();
        TryJavaFeatures.tryDistinct();
        TryJavaFeatures.tryTakeWhile();
        TryJavaFeatures.tryDropWhile();
        TryJavaFeatures.tryLimit();
        TryJavaFeatures.trySkip();
        TryJavaFeatures.tryMap();
        TryJavaFeatures.tryFlatMap();
        TryJavaFeatures.tryMatch();
    }

    public static void printTestResult(
            String javaFeatureName,  String taskDescription, boolean condition
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

        TryJavaFeatures.printTestResult(
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

        TryJavaFeatures.printTestResult(
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

        TryJavaFeatures.printTestResult(
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

        TryJavaFeatures.printTestResult(
                "Function",
                "map names of housemates to length of names",
                Arrays.equals(housemateLengths, new int[]{4, 4, 5, 6})
        );
    }

    public static void tryMethodReference(){
        int[] parsedInts = Stream.of("1", "2", "3")
                .mapToInt(Integer::parseInt)
                .toArray();

        TryJavaFeatures.printTestResult(
                "Static Method Reference",
                "convert Strings to ints",
                Arrays.equals(parsedInts, new int[]{1, 2, 3})
        );

        List<String> intStrings = IntStream.of(1, 2, 3)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());

        TryJavaFeatures.printTestResult(
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

        TryJavaFeatures.printTestResult(
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

        TryJavaFeatures.printTestResult(
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

        TryJavaFeatures.printTestResult(
                "Predicate Composing",
                "filter for positive ints",
                Arrays.equals(posMultiplesOf3LessThan10, new int[]{3, 6, 9})
        );
    }

    public static void tryComposingFunction(){
        int[] nameLengthSquaredNeg = Stream.of(
                "Jose", "Haris", "Esteban", "Sajad"
        ).mapToInt(String::length).map(
                ((IntUnaryOperator) x -> x*x).andThen(x -> -x)
        ).toArray();

        TryJavaFeatures.printTestResult(
                "Function Composing",
                "map a list of Strings to the negative square of their lengths",
                Arrays.equals(nameLengthSquaredNeg,
                        new int[]{-16, -25, -49, -25})
        );
    }

    public static void tryDistinct(){
        List<String> uniqueNames = Stream.of("Jose", "jose", "Tran", "tran")
                .map(String::toLowerCase)
                .distinct()
                .collect(Collectors.toList());

        TryJavaFeatures.printTestResult(
                "Stream::distinct",
                "get the unique strings in a stream",
                uniqueNames.equals(List.of("jose", "tran"))
        );
    }

    public static void tryTakeWhile(){
        int[] sortedInts = IntStream.rangeClosed(-3, 3).toArray();

        int[] negInts = Arrays.stream(sortedInts).takeWhile(x -> x < 0)
                .toArray();

        TryJavaFeatures.printTestResult(
                "Stream::takeWhile",
                "Efficiently select for negative ints in an ascending array",
                Arrays.equals(negInts, new int[]{-3, -2, -1})
        );
    }

    public static void tryDropWhile(){
        int[] sortedInts = IntStream.rangeClosed(-3, 3).toArray();

        int[] posInts = Arrays.stream(sortedInts).dropWhile(x -> x <= 0)
                .toArray();

        TryJavaFeatures.printTestResult(
                "Stream::dropWhile",
                "Efficiently select for positive ints in a descending array",
                Arrays.equals(posInts, new int[]{1, 2, 3})
        );
    }

    public static void tryLimit(){
        int[] firstPosMultiplesOf2 = IntStream.rangeClosed(1, 1000)
                .filter(x -> (x % 2) == 0)
                .limit(3)
                .toArray();

        TryJavaFeatures.printTestResult(
                "Stream::limit",
                "Get the first 3 multiples of 2 in a huge stream",
                Arrays.equals(firstPosMultiplesOf2, new int[]{2, 4, 6})
        );
    }

    public static void trySkip(){
        int[] lastPosMultiplesOf2 = IntStream.rangeClosed(1, 1000)
                .filter((int x) -> (x & 1) == 0)
                .skip(1000/2 - 3)
                .toArray();

        TryJavaFeatures.printTestResult(
                "Stream::skip",
                "Get the last multiples of two in a huge stream",
                Arrays.equals(lastPosMultiplesOf2, new int[]{996, 998, 1000})
        );
    }

    public static void tryMap(){
        int[] lengths = Stream.of("x", "xx", "xxx").mapToInt(String::length)
                .toArray();

        TryJavaFeatures.printTestResult(
                "Stream::map",
                "Map an array of strings to an array of their lengths",
                Arrays.equals(lengths, new int[]{1, 2, 3})
        );
    }

    public static void tryFlatMap(){
        // Because of Java's hesitation to add a primitive-specific method
        // for every possible type (in this case, flatMapToChar),
        // it's better to just use int[]
        int[] characters = Stream.of("a", "bc", "def")
                .flatMapToInt(String::chars)
                .toArray();

        TryJavaFeatures.printTestResult(
                "Stream::flatMap",
                "Map a stream of Strings to all of their characters",
                Arrays.equals(characters, "abcdef".chars().toArray())
        );
    }

    public static void tryMatch(){
        final int LONG_LEN = 3;
        String[] strs = new String[]{"123", "1", "1234567"};
        Predicate<String> isLong = s -> s.length() > LONG_LEN;

        boolean anyLongs = Arrays.stream(strs).anyMatch(isLong);
        boolean allLongs = Arrays.stream(strs).allMatch(isLong);
        boolean noLongs = Arrays.stream(strs).noneMatch(isLong);

        TryJavaFeatures.printTestResult(
                "Stream::*Match",
                "check if string array has any/all/no strings"
                        +"with length greater than 5",
                anyLongs && !allLongs && !noLongs
        );
    }
}
