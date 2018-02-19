package io.fundrequest.restapi.request;

import io.fundrequest.core.request.RequestService;
import io.fundrequest.core.request.claim.CanClaimRequest;
import io.fundrequest.core.request.claim.SignClaimRequest;
import io.fundrequest.core.request.claim.SignedClaim;
import io.fundrequest.core.request.domain.Platform;
import io.fundrequest.core.request.fund.CreateERC67FundRequest;
import io.fundrequest.core.request.view.RequestDto;
import io.fundrequest.restapi.infrastructure.AbstractRestController;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
public class RequestController extends AbstractRestController {

    private RequestService requestService;

    public RequestController(RequestService requestService) {
        this.requestService = requestService;
    }

    @GetMapping(PUBLIC_PATH + "/requests")
    public List<RequestDto> findAll() {
        return requestService.findAll();
    }

    @GetMapping(PUBLIC_PATH + "/user/requests")
    public List<RequestDto> findRequestsForUser(Principal principal) {
        return requestService.findRequestsForUser(principal);
    }

    @GetMapping(PUBLIC_PATH + "/requests/{id}")
    public RequestDto findOne(@PathVariable("id") Long id) {
        return requestService.findRequest(id);
    }

    @GetMapping({PUBLIC_PATH + "/requests/{id}/watchers", "/requests/{id}/watchlink"})
    public RequestDto findWatchers(@PathVariable("id") Long id) {
        return requestService.findRequest(id);
    }

    @GetMapping({PRIVATE_PATH + "/requests/{id}/can-claim"})
    public Boolean claimRequest(Principal principal, @PathVariable("id") Long id, @RequestParam("platform") String platform, @RequestParam("platformId") String platformId) {
        CanClaimRequest canClaimRequest = new CanClaimRequest();
        canClaimRequest.setPlatform(Platform.getPlatform(platform).get());
        canClaimRequest.setPlatformId(platformId);

        return requestService.canClaim(principal, canClaimRequest);
    }

    @PostMapping({PRIVATE_PATH + "/requests/{id}/claim"})
    public SignedClaim claimRequest(Principal principal, @RequestBody @Valid SignClaimRequest signClaimRequest, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            throw new RuntimeException("Your claim contains errors");
        }
        return requestService.signClaimRequest(principal, signClaimRequest);
    }

    @PostMapping(value = {PUBLIC_PATH + "/requests/{id}/erc67/fund"}, produces = MediaType.TEXT_PLAIN_VALUE)
    public String generateERC67ForFunding(@RequestBody @Valid CreateERC67FundRequest createERC67FundRequest) {
        return requestService.generateERC67(createERC67FundRequest);
    }

    @PutMapping(PRIVATE_PATH + "/requests/{id}/watchers")
    public ResponseEntity<?> addWatcher(@PathVariable("id") Long requestId, Principal principal) {
        requestService.addWatcherToRequest(principal, requestId);
        return ResponseEntity
                .created(getLocationFromCurrentPath(""))
                .build();
    }

    @DeleteMapping(PRIVATE_PATH + "/requests/{id}/watchers")
    public void removeWatcher(@PathVariable("id") Long requestId, Principal principal) {
        requestService.removeWatcherFromRequest(principal, requestId);
    }

}
