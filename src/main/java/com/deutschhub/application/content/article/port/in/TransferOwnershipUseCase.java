package com.deutschhub.application.content.article.port.in;

import com.deutschhub.application.content.article.dto.request.TransferOwnershipCommand;
import com.deutschhub.application.content.article.dto.response.TransferOwnershipResponse;

public interface TransferOwnershipUseCase {

    TransferOwnershipResponse transferOwnership(TransferOwnershipCommand command);
}