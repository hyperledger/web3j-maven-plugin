pragma solidity ^0.4.19;

contract Upper {
    address creator;

    constructor() {
        creator = msg.sender;
    }

}
