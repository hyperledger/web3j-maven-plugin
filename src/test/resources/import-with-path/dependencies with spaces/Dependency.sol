pragma solidity >=0.4.19 < 0.7.0;

contract Dependency {
    address creator;

    constructor() public {
        creator = msg.sender;
    }
}
