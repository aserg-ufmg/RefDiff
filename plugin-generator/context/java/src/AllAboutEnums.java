
// 1. Basic Enum
// The simplest form of an enum, containing only a list of constants.
enum Day {
    SUNDAY, MONDAY, TUESDAY, WEDNESDAY, THURSDAY, FRIDAY, SATURDAY;
}

// 2. Enum with Fields and Constructor
// Enums can have fields to store data associated with each constant.
// Constructors are used to initialize these fields. Note that enum constructors are implicitly private.
enum Planet {
    MERCURY(3.303e+23, 2.4397e6),
    VENUS(4.869e+24, 6.0518e6),
    EARTH(5.976e+24, 6.37814e6),
    MARS(6.421e+23, 3.3972e6),
    JUPITER(1.9e+27, 7.1492e7),
    SATURN(5.688e+26, 6.0268e7),
    URANUS(8.686e+25, 2.5559e7),
    NEPTUNE(1.024e+26, 2.4746e7);

    private final double mass;   // in kilograms
    private final double radius; // in meters

    Planet(double mass, double radius) {
        this.mass = mass;
        this.radius = radius;
    }

    public double getMass() {
        return mass;
    }

    public double getRadius() {
        return radius;
    }
}

// 3. Enum with Methods
// Enums can have methods, just like regular classes.
enum TrafficLight {
    RED,
    AMBER,
    GREEN;

    public TrafficLight next() {
        switch (this) {
            case RED:
                return GREEN;
            case AMBER:
                return RED;
            case GREEN:
                return AMBER;
            default:
                throw new AssertionError("Unknown traffic light: " + this);
        }
    }
}

// 4. Enum Implementing an Interface
// Enums can implement interfaces to provide a common type for different enums or classes.
interface Message {
    String getMessage();
}

enum Status implements Message {
    SUCCESS {
        @Override
        public String getMessage() {
            return "Operation was successful.";
        }
    },
    FAILURE {
        @Override
        public String getMessage() {
            return "Operation failed.";
        }
    };
}

// 5. Enum with Abstract Methods (Constant-Specific Method Implementations)
// This pattern allows each enum constant to have its own unique behavior.
enum Operation {
    PLUS {
        public double apply(double x, double y) {
            return x + y;
        }
    },
    MINUS {
        public double apply(double x, double y) {
            return x - y;
        }
    },
    TIMES {
        public double apply(double x, double y) {
            return x * y;
        }
    },
    DIVIDE {
        public double apply(double x, double y) {
            return x / y;
        }
    };

    public abstract double apply(double x, double y);
}

// 6. Enum with Static Members
// Enums can have static fields and methods.
enum Color {
    RED("#FF0000"),
    GREEN("#00FF00"),
    BLUE("#0000FF");

    private final String hexCode;
    private static int colorCount = 0;

    Color(String hexCode) {
        this.hexCode = hexCode;
        colorCount++;
    }

    public String getHexCode() {
        return hexCode;
    }

    public static int getColorCount() {
        return colorCount;
    }

    public static Color fromHex(String hex) {
        for (Color color : values()) {
            if (color.getHexCode().equalsIgnoreCase(hex)) {
                return color;
            }
        }
        return null;
    }
}

// 7. Enum with a main method to be executable
// An enum can have a main method and can be the entry point of a program.
enum Season {
    WINTER, SPRING, SUMMER, FALL;

    public static void main(String[] args) {
        System.out.println("Seasons of the year:");
        for (Season s : Season.values()) {
            System.out.println(s);
        }
    }
}

public class AllAboutEnums {

    // 8. Nested Enum (Enum inside a class)
    // Enums can be defined within a class.
    public enum Size {
        SMALL, MEDIUM, LARGE, EXTRA_LARGE
    }

    public static void main(String[] args) {
        System.out.println("--- Basic Enum ---");
        Day today = Day.MONDAY;
        System.out.println("Today is " + today);

        System.out.println("\n--- Enum with Fields and Constructor ---");
        Planet earth = Planet.EARTH;
        System.out.println("Earth's mass: " + earth.getMass() + " kg");
        System.out.println("Earth's radius: " + earth.getRadius() + " m");

        System.out.println("\n--- Enum with Methods ---");
        TrafficLight light = TrafficLight.RED;
        System.out.println("Current light: " + light);
        System.out.println("Next light: " + light.next());

        System.out.println("\n--- Enum Implementing an Interface ---");
        Status status = Status.SUCCESS;
        System.out.println("Status: " + status.getMessage());

        System.out.println("\n--- Enum with Abstract Methods ---");
        double x = 10;
        double y = 5;
        System.out.println(x + " " + Operation.PLUS + " " + y + " = " + Operation.PLUS.apply(x, y));
        System.out.println(x + " " + Operation.DIVIDE + " " + y + " = " + Operation.DIVIDE.apply(x, y));

        System.out.println("\n--- Enum with Static Members ---");
        System.out.println("Total colors defined: " + Color.getColorCount());
        Color blue = Color.fromHex("#0000FF");
        System.out.println("Found color from hex: " + blue);

        System.out.println("\n--- Nested Enum ---");
        Size shirtSize = AllAboutEnums.Size.MEDIUM;
        System.out.println("Selected shirt size: " + shirtSize);

        System.out.println("\n--- Executable Enum (Season) ---");
        Season.main(null);
    }
}
