pragma solidity >=0.4.19 <0.6.0;

contract Upper {
    address creator;

    constructor() public{
        creator = msg.sender;
    }

}
