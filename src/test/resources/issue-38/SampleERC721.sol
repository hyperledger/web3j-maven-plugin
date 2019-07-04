pragma solidity >=0.4.24 <0.6.0;

import "./ERC721.sol";
import "./ERC721Enumerable.sol";
import "./ERC721Metadata.sol";


contract SampleERC721 is ERC721, ERC721Enumerable, ERC721Metadata {

    string public constant name = "w$b3j";
    string public constant symbol = "WJ";
    uint8 public constant decimals = 0;

}