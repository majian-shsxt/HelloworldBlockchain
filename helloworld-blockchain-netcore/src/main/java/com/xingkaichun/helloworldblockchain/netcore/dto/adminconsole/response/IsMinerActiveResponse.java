package com.xingkaichun.helloworldblockchain.netcore.dto.adminconsole.response;

/**
 *
 * @author 邢开春 xingkaichun@qq.com
 */
public class IsMinerActiveResponse {

    private boolean minerInActiveState;




    //region get set

    public boolean isMinerInActiveState() {
        return minerInActiveState;
    }

    public void setMinerInActiveState(boolean minerInActiveState) {
        this.minerInActiveState = minerInActiveState;
    }


    //endregion
}