// 1. Basic class declaration
class Animal {
    // Static property (ES2022)
    static staticAnimalCount = 0;
  
    // Instance property (ES2022)
    type = "Mammal";
  
    #secretDNA = "Hidden genetic information"; // Private field (ES2022)
  
    constructor(name, species) {
        this.name = name;
        this.species = species;
        Animal.staticAnimalCount++;
        console.log(`Animal class instantiated: ${this.name} (${this.species})`);
    }
  
    // Public method
    introduce() {
        console.log(`My name is ${this.name} and I am a ${this.species}.`);
    }
  
    // Private method (ES2022)
    #getSecret() {
        return `Private information: ${this.#secretDNA}`;
    }
  
    // Public method that calls a private method
    showSecret() {
        console.log(this.#getSecret());
    }
  
    // Static method
    static describe() {
        console.log(`This is the Animal class. Current number of animals: ${Animal.staticAnimalCount}`);
    }
  
    // Getter
    get fullDescription() {
        return `${this.name} (${this.species}, Type: ${this.type})`;
    }
  
    // Setter
    set newName(name) {
        if (typeof name === 'string' && name.length > 0) {
            this.name = name;
            console.log(`Name changed to ${this.name}.`);
        } else {
            console.error("Invalid name.");
        }
    }
  }
  
  // 2. Class instantiation and usage
  const myAnimal = new Animal("Leo", "Lion");
  myAnimal.introduce();
  console.log(`Full description: ${myAnimal.fullDescription}`);
  myAnimal.newName = "Simba"; // Calling the setter
  myAnimal.introduce();
  Animal.describe(); // Calling the static method
  myAnimal.showSecret();
  // console.log(myAnimal.#secretDNA); // This should be a syntax error (inaccessible from outside)
  
  // 3. Class inheritance
  class Dog extends Animal {
    // Static property (derived class)
    static breedCount = 0;
  
    #dogTagID = Math.random().toString(36).substring(7); // プライベートフィールド
  
    constructor(name, breed) {
        super(name, "Dog"); // Calling the parent class constructor
        this.breed = breed;
        Dog.breedCount++;
        console.log(`Dog class instantiated: ${this.name} (${this.breed})`);
    }
  
    bark() {
        console.log(`${this.name} barks woof woof. ID: ${this.#dogTagID}`);
    }
  
    // Method overriding
    introduce() {
        console.log(`My name is ${this.name} and I am a ${this.breed}.`);
    }
  
    // Getter overriding
    get fullDescription() {
        return `${this.name} (${this.breed}, Type: ${this.type})`;
    }
  
    // Static method (derived class)
    static showDogStats() {
        console.log(`Currently, there are ${Dog.breedCount} dogs.`);
    }
  }
  
  const myDog = new Dog("Pochi", "Shiba Inu");
  myDog.introduce();
  myDog.bark();
  console.log(`Dog's full description: ${myDog.fullDescription}`);
  Dog.showDogStats();
  
  const anotherDog = new Dog("John", "Golden Retriever");
  anotherDog.bark();
  Dog.showDogStats();
  Animal.describe(); // Confirm that the parent class's static property is also updated
  
  // 4. Class expression
  const Cat = class extends Animal {
    constructor(name) {
        super(name, "Cat");
    }
    meow() {
        console.log(`${this.name} meows.`);
    }
  };
  
  const myCat = new Cat("Mike");
  myCat.meow();
  myCat.introduce();
  
  // 5. Mixin-like class composition (the parser only needs to handle straightforward class definitions)
  // class MyMixin {
  //     myMixinMethod() {
  //         console.log("Mixin method");
  //     }
  // }
  // class MyClassWithMixin extends MyMixin {
  //     constructor() {
  //         super();
  //         console.log("Class with Mixin");
  //     }
  // }
  // const mixedInstance = new MyClassWithMixin();
  // mixedInstance.myMixinMethod();