import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntPredicate;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class MyProcedures {
    public static void main(String[] args) {
        MyProcedures.tryPredicate();
        MyProcedures.tryConsumer();
        MyProcedures.tryFunction();
        MyProcedures.tryMethodReference();
    }

    public static void printTestResult(String description, Boolean condition){
        System.out.println(String.format(
                "%s: %s",
                condition ? "PASS" : "FAIL",
                description
        ));
    }

    public static void tryPredicate(){
        // Apparently IntPredicate is necessary for working with IntStream
        // because of differences between int and Integer.
        IntPredicate isOdd = x -> (x & 1) == 1;
        int[] oddInts = IntStream.rangeClosed(1, 10).filter(isOdd).toArray();

        MyProcedures.printTestResult(
                "Using Predicate, filter array for odd values",
                Arrays.equals(oddInts, new int[]{1, 3, 5, 7, 9})
        );
    }

    public static void tryConsumer(){
        Map<String, Boolean> housemates = new ConcurrentHashMap<>(Map.of(
                "Jose", false, "Tran", false,
                "Ahmed", false, "Ashish", false
        ));

        Consumer<Map.Entry<String, Boolean>> setToTrue =
                entry -> housemates.put(entry.getKey(), true);

        housemates.entrySet().parallelStream().forEach(setToTrue);

        MyProcedures.printTestResult(
                "Using Consumer, set all entries to true",
                housemates.values().parallelStream().allMatch(Boolean::valueOf)
        );
    }

    public static void tryFunction(){
        // We could've directly passed this Function, but let's note the type.
        Function<Integer, String> myIntToString = Object::toString;

        List<String> parsedInts = Stream.of(1, 2, 3)
                .map(myIntToString)
                .collect(Collectors.toList());

        MyProcedures.printTestResult(
                "Using Function, convert Integers to Strings",
                parsedInts.equals(List.of("1", "2", "3"))
        );

        // Primitive Specialization is necessary to avoid type casting.
        // We could've directly passed this Function, but let's note the type.
        ToIntFunction<String> toLen = String::length;

        int[] housemateLengths = Stream.of("Jose", "Tran", "Ahmed", "Ashish")
                .mapToInt(toLen)
                .toArray();

        MyProcedures.printTestResult(
                "Using Function, map names of housemates to length of names",
                Arrays.equals(housemateLengths, new int[]{4, 4, 5, 6})
        );
    }

    public static void tryMethodReference(){
        int[] parsedInts = Stream.of("1", "2", "3")
                .mapToInt(Integer::parseInt)
                .toArray();

        MyProcedures.printTestResult(
                "Using a static method reference, convert Strings to ints",
                Arrays.equals(parsedInts, new int[]{1, 2, 3})
        );

        List<String> intStrings = IntStream.of(1, 2, 3)
                .mapToObj(Integer::toString)
                .collect(Collectors.toList());

        MyProcedures.printTestResult(
                "Using an instance method reference, convert ints to Strings",
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
                "Using a bound method reference, "
                        +"count the numer of instances in a list",
                2 == new MyInt(0).countInstances(
                        Stream.of(0, 1, 0, 2).map(MyInt::new)
                                .collect(Collectors.toList())
                )
        );
    }
}
