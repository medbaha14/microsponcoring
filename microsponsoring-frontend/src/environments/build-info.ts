// This file contains build information for the application
// Update these values manually or use a build script to populate them

export const buildInfo = {
  // Application version - update this for each release
  version: '1.0.0',
  
  // Build timestamp - set this during build or deployment
  buildTime: new Date().toISOString(),
  
  // Environment - set this based on your deployment
  environment: 'development', // Change to 'staging' or 'production' as needed
  
  // Build number - can be set from CI/CD pipeline or manually
  buildNumber: 'local',
  
  // Git information - can be populated during build
  git: {
    commit: 'unknown',
    branch: 'unknown',
    tag: 'none'
  },
  
  // Last update - when the application was last deployed
  lastUpdate: new Date().toISOString(),
  
  // Build configuration
  buildConfig: {
    optimization: false,
    sourceMaps: true,
    minification: false
  }
};

// Helper function to get formatted build time
export function getFormattedBuildTime(): string {
  try {
    const buildDate = new Date(buildInfo.buildTime);
    return buildDate.toLocaleString('en-US', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit',
      second: '2-digit',
      timeZoneName: 'short'
    });
  } catch {
    return buildInfo.buildTime;
  }
}

// Helper function to get environment display name
export function getEnvironmentDisplayName(): string {
  switch (buildInfo.environment) {
    case 'production':
      return 'Production';
    case 'staging':
      return 'Staging';
    case 'development':
      return 'Development';
    default:
      return buildInfo.environment.charAt(0).toUpperCase() + buildInfo.environment.slice(1);
  }
}

// Helper function to get version display string
export function getVersionDisplay(): string {
  return `v${buildInfo.version} (${buildInfo.buildNumber})`;
}

// Environment-specific configurations
export const environmentConfigs = {
  development: {
    apiUrl: 'http://localhost:8080/api',
    production: false,
    enableDebug: true
  },
  staging: {
    apiUrl: '/api',
    production: true,
    enableDebug: false
  },
  production: {
    apiUrl: '/api',
    production: true,
    enableDebug: false
  }
};

// Get current environment config
export function getCurrentEnvironmentConfig() {
  return environmentConfigs[buildInfo.environment as keyof typeof environmentConfigs] || environmentConfigs.development;
}
