pragma solidity >=0.4.19 <0.6.0;


library ConvertLib {
    function convert(uint amount, uint conversionRate) pure public returns (uint convertedAmount)
    {
        return amount * conversionRate;
    }
}