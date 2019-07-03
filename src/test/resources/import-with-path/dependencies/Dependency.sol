pragma solidity >=0.4.19 <0.6.0;

contract Dependency {
    address creator;

    constructor() public {
        creator = msg.sender;
    }
}
