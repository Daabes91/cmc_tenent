/**
 * Utility function to convert route paths to admin page names
 * Handles dynamic routes for edit/new pages and supports all existing admin routes
 */
export function getPageNameFromRoute(path: string): string {
  const routeNameMap: Record<string, string> = {
    '/': 'Dashboard',
    '/appointments': 'Appointments',
    '/calendar': 'Calendar',
    '/doctors': 'Doctors',
    '/patients': 'Patients',
    '/treatment-plans': 'Treatment Plans',
    '/materials': 'Materials',
    '/services': 'Services',
    '/insurance-companies': 'Insurance Companies',
    '/blogs': 'Blogs',
    '/reports': 'Reports',
    '/staff': 'Staff',
    '/clinic-settings': 'Clinic Settings',
    '/settings': 'Settings',
    '/paypal-transactions': 'PayPal Transactions'
  };
  
  // Handle dynamic routes for new pages
  if (path.includes('/new')) {
    const basePath = path.replace('/new', '');
    const baseName = routeNameMap[basePath];
    if (baseName) {
      // Convert plural to singular for "New X" format
      const singularName = getSingularName(baseName);
      return `New ${singularName}`;
    }
  }
  
  // Handle dynamic routes for edit pages (with ID)
  if (path.match(/\/\d+$/)) {
    const basePath = path.replace(/\/\d+$/, '');
    const baseName = routeNameMap[basePath];
    if (baseName) {
      // Convert plural to singular for "Edit X" format
      const singularName = getSingularName(baseName);
      return `Edit ${singularName}`;
    }
  }
  
  // Handle dynamic routes with UUID or other patterns
  if (path.match(/\/[a-f0-9-]{36}$/i)) {
    const basePath = path.replace(/\/[a-f0-9-]{36}$/i, '');
    const baseName = routeNameMap[basePath];
    if (baseName) {
      const singularName = getSingularName(baseName);
      return `Edit ${singularName}`;
    }
  }
  
  // Return mapped name or default to Dashboard
  return routeNameMap[path] || 'Dashboard';
}

/**
 * Helper function to convert plural page names to singular
 */
function getSingularName(pluralName: string): string {
  const singularMap: Record<string, string> = {
    'Appointments': 'Appointment',
    'Doctors': 'Doctor',
    'Patients': 'Patient',
    'Treatment Plans': 'Treatment Plan',
    'Materials': 'Material',
    'Services': 'Service',
    'Insurance Companies': 'Insurance Company',
    'Blogs': 'Blog',
    'Reports': 'Report',
    'Staff': 'Staff Member',
    'PayPal Transactions': 'PayPal Transaction'
  };
  
  return singularMap[pluralName] || pluralName;
}