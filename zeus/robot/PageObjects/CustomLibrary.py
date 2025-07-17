from datetime import date
from pyxirr import xirr

def custom_xirr(dates,amounts):
   xirr_value= xirr(dates,amounts)
   return xirr_value


def custom_modify_number(a): 
   a= str(a)
   a=(a.replace(",", ""))
   a = float(a)
   return a

