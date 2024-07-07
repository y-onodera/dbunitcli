export const environment = {
    production: false,
    apiUrl: 'http://localhost',
    apiPort: 8080,
    serverUrl() {
      return `${this.apiUrl}:${this.apiPort}/dbunit-cli/`;
    }
  };