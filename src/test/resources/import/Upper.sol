pragma solidity >=0.4.19;

contract Upper {
    address creator;

    constructor() public{
        creator = msg.sender;
    }

}
