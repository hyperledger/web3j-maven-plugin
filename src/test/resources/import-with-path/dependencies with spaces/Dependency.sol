pragma solidity >=0.4.19;

contract Dependency {
    address creator;

    constructor() public {
        creator = msg.sender;
    }
}
