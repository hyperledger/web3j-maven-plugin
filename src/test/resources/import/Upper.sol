pragma solidity ^0.4.18;

contract Upper {
    address creator;

    constructor() {
        creator = msg.sender;
    }

}
