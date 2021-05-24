pragma solidity >=0.4.19 < 0.7.0;

contract Issue17relative2 {
    address creator;

    constructor() public {
        creator = msg.sender;
    }

}
