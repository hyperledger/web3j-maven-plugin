pragma solidity ^0.4.18;

contract Upper {
    address creator;

    function Upper() {
        creator = msg.sender;
    }

}
