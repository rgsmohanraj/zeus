/** Angular Imports */
import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { MatLegacyDialog as MatDialog } from '@angular/material/legacy-dialog';

/** Custom Services */
import { ProductsService } from 'app/products/products.service';

/** Custom Components */
import { DeleteDialogComponent } from '../../../shared/delete-dialog/delete-dialog.component';

/**
 * View Charge Component.
 */
@Component({
  selector: 'mifosx-view-charge',
  templateUrl: './view-charge.component.html',
  styleUrls: ['./view-charge.component.scss']
})
export class ViewChargeComponent implements OnInit {

  /** Charge data. */
  chargeData: any;
  gstData:any;
  chargetype:any;


  /**
   * Retrieves the charge data from `resolve`.
   * @param {ProductsService} productsService Products Service.
   * @param {ActivatedRoute} route Activated Route.
   * @param {Router} router Router for navigation.
   * @param {MatDialog} dialog Dialog reference.
   */
  constructor(private productsService: ProductsService,
              private route: ActivatedRoute,
              private router: Router,
              private dialog: MatDialog) {
    this.route.data.subscribe((data: { charge: any }) => {
      this.chargeData = data.charge;
//       this.chargetype=data.charge.chargeType;
     console.log(this.chargeData,"this.chargeData");
console.log(this.chargeData.gstSlabLimitApplyForSelected.value ,"gstSlabLimitApplyForSelected");
      this.gstData=data.charge;
      console.log(this.gstData,"this.gstData");
    });
  }

  ngOnInit() {
  }

  /**
   * Deletes the charge and redirects to charges.
   */
  deleteCharge() {
    const deleteChargeDialogRef = this.dialog.open(DeleteDialogComponent, {
      data: { deleteContext: `charge ${this.chargeData.name}` }
    });
    deleteChargeDialogRef.afterClosed().subscribe((response: any) => {
      if (response.delete) {
        this.productsService.deleteCharge(this.chargeData.id)
          .subscribe(() => {
            this.router.navigate(['/products/charges']);
          });
      }
    });
  }

}
