/** Angular Imports */
import { Component, OnInit, ViewChild } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

/** Custom Components */
import { LoanProductDetailsStepComponent } from '../loan-product-stepper/loan-product-details-step/loan-product-details-step.component';
import { LoanProductCurrencyStepComponent } from '../loan-product-stepper/loan-product-currency-step/loan-product-currency-step.component';
import { LoanProductTermsStepComponent } from '../loan-product-stepper/loan-product-terms-step/loan-product-terms-step.component';
import { LoanProductSettingsStepComponent } from '../loan-product-stepper/loan-product-settings-step/loan-product-settings-step.component';
import { LoanProductChargesStepComponent } from '../loan-product-stepper/loan-product-charges-step/loan-product-charges-step.component';
import { LoanProductAccountingStepComponent } from '../loan-product-stepper/loan-product-accounting-step/loan-product-accounting-step.component';
import { LoanProductSignatureStepComponent } from '../loan-product-stepper/loan-product-signature-step/loan-product-signature-step.component';
import { LoanProductServicerFeeStepComponent } from '../loan-product-stepper/loan-product-servicer-fee-step/loan-product-servicer-fee-step.component';
import { LoanProductCollectionStepComponent } from '../loan-product-stepper/loan-product-collection-step/loan-product-collection-step.component';

/** Custom Services */
import { ProductsService } from 'app/products/products.service';
import { SettingsService } from 'app/settings/settings.service';
import { NotificationService } from '../../../notification.service';

@Component({
  selector: 'mifosx-create-loan-product',
  templateUrl: './create-loan-product.component.html',
  styleUrls: ['./create-loan-product.component.scss']
})
export class CreateLoanProductComponent implements OnInit {

  @ViewChild(LoanProductDetailsStepComponent, { static: true }) loanProductDetailsStep: LoanProductDetailsStepComponent;
  @ViewChild(LoanProductCurrencyStepComponent, { static: true }) loanProductCurrencyStep: LoanProductCurrencyStepComponent;
  @ViewChild(LoanProductTermsStepComponent, { static: true }) loanProductTermsStep: LoanProductTermsStepComponent;
  @ViewChild(LoanProductSettingsStepComponent, { static: true }) loanProductSettingsStep: LoanProductSettingsStepComponent;
  @ViewChild(LoanProductChargesStepComponent, { static: true }) loanProductChargesStep: LoanProductChargesStepComponent;
  @ViewChild(LoanProductAccountingStepComponent, { static: true }) loanProductAccountingStep: LoanProductAccountingStepComponent;
  @ViewChild(LoanProductSignatureStepComponent, { static: true }) loanProductSignatureStep: LoanProductSignatureStepComponent;
  @ViewChild(LoanProductServicerFeeStepComponent, { static: true }) loanProductServicerFeeStep: LoanProductServicerFeeStepComponent;
  @ViewChild(LoanProductCollectionStepComponent,{static:true}) loanProductCollectionStep: LoanProductCollectionStepComponent;

  loanProductsTemplate: any;
  accountingRuleData = ['None', 'Cash', 'Accrual (periodic)', 'Accrual (upfront)'];

  /**
   * @param {ActivatedRoute} route Activated Route.
   * @param {ProductsService} productsService Product Service.
   * @param {SettingsService} settingsService Settings Service
   * @param {Router} router Router for navigation.
   */

  constructor(private route: ActivatedRoute,
    private productsService: ProductsService,
    private settingsService: SettingsService,
    private router: Router,
    private notifyService: NotificationService) {
    this.route.data.subscribe((data: { loanProductsTemplate: any }) => {
      this.loanProductsTemplate = data.loanProductsTemplate;
      const assetAccountData = this.loanProductsTemplate.accountingMappingOptions.assetAccountOptions || [];
      const liabilityAccountData = this.loanProductsTemplate.accountingMappingOptions.liabilityAccountOptions || [];
      this.loanProductsTemplate.accountingMappingOptions.assetAndLiabilityAccountOptions = assetAccountData.concat(liabilityAccountData);
    });
  }

  ngOnInit() 
  
  {
    

  }

  get loanProductDetailsForm() {
    return this.loanProductDetailsStep.loanProductDetailsForm;
  }

  get loanProductCurrencyForm() {
    return this.loanProductCurrencyStep.loanProductCurrencyForm;
  }

  get loanProductTermsForm() {
    return this.loanProductTermsStep.loanProductTermsForm;
  }

  get loanProductSettingsForm() {
    return this.loanProductSettingsStep.loanProductSettingsForm;
  }

  get loanProductSignatureForm() {
    return this.loanProductSignatureStep.loanProductSignatureForm;
  }

  get loanProductAccountingForm() {
    return this.loanProductAccountingStep.loanProductAccountingForm;
  }

  get loanProductServicerFeeForm() {
    return this.loanProductServicerFeeStep.loanProductServicerFeeForm;
  }

  get loanProductCollectionForm() {
    return this.loanProductCollectionStep.loanProductCollectionForm;
  }

    get loanProductFormValid() {
     return (
      this.loanProductDetailsForm.valid &&
      this.loanProductCurrencyForm.valid &&
      this.loanProductTermsForm.valid &&
      this.loanProductSettingsForm.valid &&
      this.loanProductSignatureForm.valid &&
      this.loanProductServicerFeeForm.valid &&
      this.loanProductAccountingForm.valid &&
      this.loanProductCollectionForm.valid
    );
  }

  get loanServiceStep() {
    return {
      ...this.loanProductServicerFeeStep.loanProductServicerFee,
    };
  }
  get loanProduct() {
    return {
      ...this.loanProductDetailsStep.loanProductDetails,
      ...this.loanProductCurrencyStep.loanProductCurrency,
      ...this.loanProductTermsStep.loanProductTerms,
      ...this.loanProductSettingsStep.loanProductSettings,
      ...this.loanProductSignatureStep.loanProductSignature,
      ...this.loanProductChargesStep.loanProductCharges,
      ...this.loanProductAccountingStep.loanProductAccounting,
      ...this.loanProductCollectionStep.loanProductCollectionAppropriation
    
    };
  }

  submit() {
    // TODO: Update once language and date settings are setup
    const dateFormat = this.settingsService.dateFormat;
    const loanProduct = {
      ...this.loanProduct,
      charges: this.loanProduct.charges.map((charge: any) => ({ id: charge.id })),
      dateFormat,
      servicerFeeInterestConfigEnabled: this.loanServiceStep.servicerFeeInterestConfigEnabled,
      servicerFeeChargesConfigEnabled: this.loanServiceStep.servicerFeeChargesConfigEnabled,
      locale: this.settingsService.language.code

    };
    delete loanProduct.allowAttributeConfiguration;
    delete loanProduct.advancedAccountingRules;
    this.productsService.createLoanProduct(loanProduct)
      .subscribe((response: any) => {
        const loanServiceData = {
          ...this.loanServiceStep,
          productId: response.resourceId,
          dateFormat,
          locale: this.settingsService.language.code
        };
        //           console.log(this.loanServiceStep,"this.loanServiceStep");
        delete loanServiceData.servicerFeeInterestConfigEnabled;
        delete loanServiceData.servicerFeeChargesConfigEnabled;
        this.productsService.createServicerFeeLoanProduct(loanServiceData)
          .subscribe((response: any) => {
            this.router.navigate(['../', response.resourceId], { relativeTo: this.route });
          }, error => {
            for (let i = 0; i < error.error.errors.length; i++) {
              this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')
            }
          });
      }, error => {
        for (let i = 0; i < error.error.errors.length; i++) {
          this.notifyService.showError(error.error.errors[i].developerMessage, 'Invalid')

        }
      });
  }

}
