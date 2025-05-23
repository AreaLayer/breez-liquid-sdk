// This file is automatically generated, so please do not edit it.
// @generated by `flutter_rust_bridge`@ 2.9.0.

// ignore_for_file: invalid_use_of_internal_member, unused_import, unnecessary_import

import 'frb_generated.dart';
import 'package:flutter_rust_bridge/flutter_rust_bridge_for_generated.dart';
import 'package:freezed_annotation/freezed_annotation.dart' hide protected;
part 'error.freezed.dart';

@freezed
sealed class PaymentError with _$PaymentError implements FrbException {
  const PaymentError._();

  const factory PaymentError.alreadyClaimed() = PaymentError_AlreadyClaimed;
  const factory PaymentError.alreadyPaid() = PaymentError_AlreadyPaid;
  const factory PaymentError.paymentInProgress() = PaymentError_PaymentInProgress;
  const factory PaymentError.amountOutOfRange({required BigInt min, required BigInt max}) =
      PaymentError_AmountOutOfRange;
  const factory PaymentError.amountMissing({required String err}) = PaymentError_AmountMissing;
  const factory PaymentError.assetError({required String err}) = PaymentError_AssetError;
  const factory PaymentError.invalidNetwork({required String err}) = PaymentError_InvalidNetwork;
  const factory PaymentError.generic({required String err}) = PaymentError_Generic;
  const factory PaymentError.invalidOrExpiredFees() = PaymentError_InvalidOrExpiredFees;
  const factory PaymentError.insufficientFunds() = PaymentError_InsufficientFunds;
  const factory PaymentError.invalidDescription({required String err}) = PaymentError_InvalidDescription;
  const factory PaymentError.invalidInvoice({required String err}) = PaymentError_InvalidInvoice;
  const factory PaymentError.invalidPreimage() = PaymentError_InvalidPreimage;
  const factory PaymentError.pairsNotFound() = PaymentError_PairsNotFound;
  const factory PaymentError.paymentTimeout() = PaymentError_PaymentTimeout;
  const factory PaymentError.persistError() = PaymentError_PersistError;
  const factory PaymentError.receiveError({required String err}) = PaymentError_ReceiveError;
  const factory PaymentError.refunded({required String err, required String refundTxId}) =
      PaymentError_Refunded;
  const factory PaymentError.selfTransferNotSupported() = PaymentError_SelfTransferNotSupported;
  const factory PaymentError.sendError({required String err}) = PaymentError_SendError;
  const factory PaymentError.signerError({required String err}) = PaymentError_SignerError;
}

@freezed
sealed class SdkError with _$SdkError implements FrbException {
  const SdkError._();

  const factory SdkError.alreadyStarted() = SdkError_AlreadyStarted;
  const factory SdkError.generic({required String err}) = SdkError_Generic;
  const factory SdkError.notStarted() = SdkError_NotStarted;
  const factory SdkError.serviceConnectivity({required String err}) = SdkError_ServiceConnectivity;
}
