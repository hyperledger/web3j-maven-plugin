pragma solidity >=10.0.0;

contract HelloWorld {

    string saySomething;

    constructor() public  {
        saySomething = "Hello World!";
    }

    function speak() public view returns (string memory) {
        return saySomething;
    }

    function saySomethingElse(string memory newSaying) public returns (bool success) {
        saySomething = newSaying;
        return true;
    }

}