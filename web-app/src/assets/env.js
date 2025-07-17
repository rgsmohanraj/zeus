(function(window) {
  window["env"] = window["env"] || {};

  // BackEnd Environment variables
  window["env"]["fineractApiUrls"] = 'http://localhost:8443';
  window["env"]["fineractApiUrl"]  = 'http://localhost:8443';

  window["env"]["apiProvider"] = '/lms/api';
  window["env"]["apiVersion"]  = '/v1';

  window["env"]["fineractPlatformTenantId"]  = '';

  // Language Environment variables
  window["env"]["defaultLanguage"] = '';
  window["env"]["supportedLanguages"] = '';
})(this);
