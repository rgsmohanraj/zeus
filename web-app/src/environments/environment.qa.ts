// `.env.ts` is generated by the `npm run env` command
import env from './.env';

export const environment = {
  production: true,
  version: env.mifos_x.version,
  hash: env.mifos_x.hash,
  // For connecting to server running elsewhere update the tenant identifier
  zeusPlatformTenantId: 'zeus-colending',
  // For connecting to others servers running elsewhere update the base API URL
  baseApiUrls: 'https://zeus-qa.vivriticapital.com', 
  // For connecting to server running elsewhere set the base API URL
  //baseApiUrl: window['env']['fineractApiUrl'] || 'https://demo.fineract.dev',
  baseApiUrl: 'https://zeus-qa.vivriticapital.com', 
  allowServerSwitch: env.allow_switching_backend_instance,
  apiProvider: '/lms/api',
  apiVersion: '/v1',
  serverUrl: '',
  oauth: {
    enabled: true,  // For connecting to Mifos X using OAuth2 Authentication change the value to true
    serverUrl: ''
  },
  defaultLanguage: 'en-US',
  supportedLanguages: 'en-US,fr-FR',
  enableLogoutOnUserInActive: true,
  logoutOnUserInActiveAfter: 20, //in minutes
  secret_key:'nv7IFESZ3GasMZVA',
  IV:'d8g&l4b2#6^cki3m',
};

// Server URL
environment.serverUrl = `${environment.baseApiUrl}${environment.apiProvider}${environment.apiVersion}`;
console.log("Loading environment.qa.ts - server URL: ", environment.serverUrl);
environment.oauth.serverUrl = `${environment.baseApiUrl}${environment.apiProvider}`;
