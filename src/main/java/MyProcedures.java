import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.IntPredicate;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

public class MyProcedures {
    public static void main(String[] args) {
        System.out.println("Hello World!");

        MyProcedures.tryPredicate();
        MyProcedures.tryConsumer();
        MyProcedures.tryFunction();
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
        int[] oddInts = IntStream.rangeClosed(0, 10).filter(isOdd).toArray();

        MyProcedures.printTestResult("filtering array for odd values",
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

        MyProcedures.printTestResult("set all entries to true",
                housemates.values().parallelStream().allMatch(Boolean::valueOf)
        );
    }

    public static void tryFunction(){
        List<String> housemates = List.of("Jose", "Tran", "Ahmed", "Ashish");

        // Primitive Specialization necessary
        ToIntFunction<String> toLen = String::length;
        int[] housemateLengths = housemates.parallelStream().mapToInt(toLen)
                .toArray();

        MyProcedures.printTestResult(
                "map names of housemates to length of names",
                Arrays.equals(housemateLengths, new int[]{4, 4, 5, 6})
        );
    }
}
