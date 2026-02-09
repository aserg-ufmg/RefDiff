# Comprehensive Guide to Ruby Method Definitions

### 1. Basic Method Definition with `def` Keyword

class MyClass
  # Instance method
  def instance_method_example
    puts "This is an instance method."
  end

  # Class method (self.method_name)
  def self.class_method_example
    puts "This is a class method."
  end
end

  # Class method (ClassName.method_name)
  # Can also be defined within class << self

class << MyClass
  def another_class_method_example
    puts "This is also a class method."
  end
end

puts "--- 1. Basic Method Definition with `def` Keyword ---"
obj = MyClass.new
obj.instance_method_example

MyClass.class_method_example
MyClass.another_class_method_example
puts

### 2. Defining Singleton Methods

obj1 = String.new("hello")
obj2 = String.new("world")

# Define a singleton method only for obj1
def obj1.greet
  puts "#{self}, hello!"
end

puts "--- 2. Defining Singleton Methods ---"
obj1.greet # => "hello, hello!"
# obj2.greet # => NoMethodError: undefined method `greet' for "world":String

# Singleton method for a class (another way to define class methods)
class MySpecificClass
end

def MySpecificClass.class_specific_method
  puts "This is a method specific to MySpecificClass."
end

MySpecificClass.class_specific_method
puts

### 3. Dynamic Method Definition with `define_method`

class DynamicMethods
  # Dynamically define an instance method
  define_method :dynamic_instance_method do |name|
    puts "Hello, #{name}! (Dynamic instance method)"
  end

  # Dynamically define a class method
  define_singleton_method :dynamic_class_method do |message|
    puts "Message: #{message} (Dynamic class method)"
  end

  # Define multiple methods in a loop
  ["method_a", "method_b"].each do |name|
    define_method name do
      puts "This is #{name}."
    end
  end
end

puts "--- 3. Dynamic Method Definition with `define_method` ---"
dm = DynamicMethods.new
dm.dynamic_instance_method("Ruby")
dm.method_a
dm.method_b

DynamicMethods.dynamic_class_method("Test")
puts

### 4. Defining Accessor Methods with `attr_accessor`, `attr_reader`, `attr_writer`

class User
  attr_accessor :name
  attr_reader :email
  attr_writer :age

  def initialize(name, email, age)
    @name = name
    @email = email
    @age = age
  end

  def display_age
    puts "Age: #{@age}"
  end
end

puts "--- 4. Defining Accessor Methods with `attr_accessor`, `attr_reader`, `attr_writer` ---"
user = User.new("Alice", "alice@example.com", 30)

puts user.name
user.name = "Bob"
puts user.name

puts user.email
# user.email = "bob@example.com" # NoMethodError

# user.age # NoMethodError
user.age = 31
user.display_age
puts

### 5. Defining Method Aliases with `alias_method`

class Greeter
  def hello
    puts "Hello!"
  end

  alias_method :hi, :hello
end

puts "--- 5. Defining Method Aliases with `alias_method` ---"
g = Greeter.new
g.hello
g.hi
puts

### 6. Handling Undefined Methods with `method_missing`

class GhostMethods
  def method_missing(method_name, *args, &block)
    if method_name.to_s.start_with?("say_")
      message = method_name.to_s.split("_", 2)[1]
      puts "You want to say '#{message}', right? Arguments: #{args.join(', ')}"
      block.call if block_given?
    else
      super
    end
  end

  def respond_to_missing?(method_name, include_private = false)
    method_name.to_s.start_with?("say_") || super
  end
end

puts "--- 6. Handling Undefined Methods with `method_missing` ---"
gm = GhostMethods.new
gm.say_hello("Ruby", "Gemini") do
  puts "Block was executed."
end
gm.say_goodbye

puts "gm.respond_to?(:say_anything): #{gm.respond_to?(:say_anything)}"
puts "gm.respond_to?(:unknown_method): #{gm.respond_to?(:unknown_method)}"
puts

### 7. Using `Proc` or `lambda` like methods (not strictly method definitions)

class ProcRunner
  MY_PROC = proc { |x| puts "Proc executed: #{x}" }
  MY_LAMBDA = ->(y) { puts "Lambda executed: #{y}" }

  def initialize
    @instance_lambda = ->(z) { puts "Instance Lambda executed: #{z} (self: #{self.class})" }
  end

  def run_proc(val)
    MY_PROC.call(val)
  end

  def run_lambda(val)
    MY_LAMBDA.call(val)
  end

  def run_instance_lambda(val)
    @instance_lambda.call(val)
  end

  def self.run_class_proc(val)
    MY_PROC.call(val)
  end
end

puts "--- 7. Using `Proc` or `lambda` like methods ---"
runner = ProcRunner.new
runner.run_proc(10)
runner.run_lambda(20)
runner.run_instance_lambda(30)
ProcRunner.run_class_proc(40)
puts

### Method Visibility

class VisibilityDemo
  def public_method
    puts "This is a public method."
    private_method_called_from_public
    protected_method_called_from_public
  end

  protected

  def protected_method
    puts "This is a protected method."
  end

  def protected_method_called_from_public
    puts "Protected method called from public method"
  end

  private

  def private_method
    puts "This is a private method."
  end

  def private_method_called_from_public
    puts "Private method called from public method"
  end
end

class SubVisibilityDemo < VisibilityDemo
  def call_protected_from_subclass(other)
    other.protected_method
  end

  def call_own_private
    private_method
  end
end

puts "--- Method Visibility ---"
demo = VisibilityDemo.new
demo.public_method

sub_demo1 = SubVisibilityDemo.new
sub_demo2 = SubVisibilityDemo.new
sub_demo1.call_protected_from_subclass(sub_demo2)
sub_demo1.call_own_private
puts

### 8. Top-Level Methods (Methods not belonging to a class)

# Methods defined at the top level of a file using `def`
# are defined as private methods of Ruby's default object, `main`
# (an instance of the Object class).
# This allows them to behave like global functions, callable
# directly from anywhere in the program.

def top_level_method_example(name)
  puts "Hello, #{name}! (Top-level method)"
end

puts "--- 8. Top-Level Methods ---"
top_level_method_example("Global")

# Cannot be called with an explicit `self` (because it's a private method)
# self.top_level_method_example("Error") # => NoMethodError: private method `top_level_method_example' called for main:Object

# The following is a conceptual explanation of how top-level methods are interpreted:
# class Object
#   private
#   def top_level_method_example(name)
#     puts "Hello, #{name}! (Top-level method)"
#   end
# end
puts

### 9. Advanced Parameter Types

class AdvancedParameters
  # Method with optional parameter
  def optional_param_method(a = 1)
  end

  # Method with keyword parameter
  def keyword_param_method(a: "default")
  end

  # Method with hash splat parameter
  def hash_splat_param_method(**options)
  end

  # Combination of various parameters
  def all_param_types(required, optional = "default", *splat, keyword_req:, keyword_opt: "opt", **hash_splat, &block)
  end
end
