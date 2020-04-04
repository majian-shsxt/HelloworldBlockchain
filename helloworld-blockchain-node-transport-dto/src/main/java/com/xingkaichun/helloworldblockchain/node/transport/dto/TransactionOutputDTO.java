package com.xingkaichun.helloworldblockchain.node.transport.dto;


import lombok.Data;

import java.io.Serializable;

/**
 * 交易输出
 */
@Data
public class TransactionOutputDTO implements Serializable {

    //交易输出的ID
    private String transactionOutputUUID;
    //交易输出的地址
    private String address;
    //交易输出的金额
    private String value;

}