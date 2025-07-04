import UserNotifications
import Foundation

struct LnurlInfoRequest: Codable {
    let callback_url: String
    let reply_url: String
}

struct LnurlInfoResponse: Decodable, Encodable {
    let callback: String
    let maxSendable: UInt64
    let minSendable: UInt64
    let metadata: String
    let commentAllowed: Int
    let tag: String
    
    init(callback: String, maxSendable: UInt64, minSendable: UInt64, metadata: String, commentAllowed: Int, tag: String) {
        self.callback = callback
        self.maxSendable = maxSendable
        self.minSendable = minSendable
        self.metadata = metadata
        self.commentAllowed = commentAllowed
        self.tag = tag
    }
}

class LnurlPayInfoTask : LnurlPayTask {
    fileprivate let TAG = "LnurlPayInfoTask"
    
    init(payload: String, logger: ServiceLogger, contentHandler: ((UNNotificationContent) -> Void)? = nil, bestAttemptContent: UNMutableNotificationContent? = nil) {
        let successNotificationTitle = ResourceHelper.shared.getString(key: Constants.LNURL_PAY_INFO_NOTIFICATION_TITLE, fallback: Constants.DEFAULT_LNURL_PAY_INFO_NOTIFICATION_TITLE)
        let failNotificationTitle = ResourceHelper.shared.getString(key: Constants.LNURL_PAY_NOTIFICATION_FAILURE_TITLE, fallback: Constants.DEFAULT_LNURL_PAY_NOTIFICATION_FAILURE_TITLE)
        super.init(payload: payload, logger: logger, contentHandler: contentHandler, bestAttemptContent: bestAttemptContent, successNotificationTitle: successNotificationTitle, failNotificationTitle: failNotificationTitle)
    }
    
    override func start(liquidSDK: BindingLiquidSdk) throws {
        var request: LnurlInfoRequest? = nil
        do {
            request = try JSONDecoder().decode(LnurlInfoRequest.self, from: self.payload.data(using: .utf8)!)
        } catch let e {
            self.logger.log(tag: TAG, line: "failed to decode payload: \(e)", level: "ERROR")
            self.displayPushNotification(title: self.failNotificationTitle, logger: self.logger, threadIdentifier: Constants.NOTIFICATION_THREAD_REPLACEABLE)
            throw e
        }
        
        do {
            // Get the lightning limits
            let limits = try liquidSDK.fetchLightningLimits()
            // Max millisatoshi amount LN SERVICE is willing to receive
            let maxSendableMsat = limits.receive.maxSat * UInt64(1000)
            // Min millisatoshi amount LN SERVICE is willing to receive, can not be less than 1 or more than `maxSendableMsat`
            let minSendableMsat = limits.receive.minSat * UInt64(1000)
            if minSendableMsat < UInt64(1) || minSendableMsat > maxSendableMsat {
                throw InvalidLnurlPayError.minSendable
            }
            // Format the response
            let plainTextMetadata = ResourceHelper.shared.getString(key: Constants.LNURL_PAY_METADATA_PLAIN_TEXT, fallback: Constants.DEFAULT_LNURL_PAY_METADATA_PLAIN_TEXT)
            let metadata = "[[\"text/plain\",\"\(plainTextMetadata)\"]]"
            replyServer(encodable: LnurlInfoResponse(callback: request!.callback_url,
                                                     maxSendable: maxSendableMsat,
                                                     minSendable: minSendableMsat,
                                                     metadata: metadata,
                                                     commentAllowed: Constants.LNURL_PAY_COMMENT_MAX_LENGTH,
                                                     tag: "payRequest"),
                        replyURL: request!.reply_url,
                        maxAge: Constants.CACHE_CONTROL_MAX_AGE_DAY)
        } catch let e {
            self.logger.log(tag: TAG, line: "failed to process lnurl: \(e)", level: "ERROR")
            fail(withError: e.localizedDescription, replyURL: request!.reply_url)
        }
    }
}
