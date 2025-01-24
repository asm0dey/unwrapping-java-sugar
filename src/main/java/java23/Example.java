package java23;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.logging.Logger;

import static java.lang.String.format;
import static java.lang.System.setProperty;
import static java.util.logging.Logger.getLogger;

public class Example {
    private static final Logger logger = getLogger(Example.class.getName());

    /// Marker interface to mark something as a business entity
    ///
    /// Intended usage is to put them into a collection and handle like
    /// {@snippet :
    /// List<Entity> lst = List.of();
    /// for (Entity item: lst){
    ///   switch (item){
    ///     case Snickers snickers -> System.out.println(snickers.name);
    ///     default -> handle(item);
    ///   }
    /// }
    ///}
    @SuppressWarnings("SwitchStatementWithTooFewBranches")
    sealed interface Entity {
        private String type() {
            return "Entity";
        }

        default String humanReadableTypeName() {
            return type();
        }
    }

    /// A trivial class describing Person
    ///
    /// @param age - the age __rounded up__
    record Person(String name, int age) implements Entity {
        @Override
        public String humanReadableTypeName() {
            return "Person";
        }
    }

    abstract sealed static class NamedEntity<T> implements Entity {
        abstract String getName();

        abstract T payload();
    }

    static final class Snickers extends NamedEntity<String> {
        private final String name;
        private final String producer;

        Snickers(String name, String producer) {
            this.name = name;
            this.producer = producer;
        }

        @Override
        String payload() {
            return producer;
        }

        @Override
        public String getName() {
            return name;
        }

        public String getProducer() {
            return producer;
        }

        @Override
        public String humanReadableTypeName() {
            return "Snickers";
        }
    }

    private static final List<Entity> entities = List.of(
            new Person("John", 23),
            new Person("Jane", 22),
            new Snickers("Air Jordan", "Nike")
    );

    public static void main(String[] args) throws IOException {
        var format = Files.readAllLines(Paths.get(args[0])).getFirst();
        setProperty("java.util.logging.SimpleFormatter.format", format);
        List<Entity> reversedEntities = entities.reversed();
        reversedEntities
                .stream()
                .map(e -> switch (e) {
                    case Person p -> format("%s is %d years old", p.name, p.age);
                    case Snickers s -> format("""
                            We have snickers "%s"
                            They're produced by "%s"
                            """, s.name, s.payload());
                    case NamedEntity<?> ne ->
                            format("It's a named entity %s with payload %s", ne.getName(), ne.payload());
                })
                .forEach(x -> logger.info(() -> x));
    }
}