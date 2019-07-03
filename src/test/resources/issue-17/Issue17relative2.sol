pragma solidity >=0.4.19 <0.6.0;

contract Issue17relative2 {
    address creator;

    constructor() public {
        creator = msg.sender;
    }

}
