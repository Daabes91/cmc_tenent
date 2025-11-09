# SAAS Manager Admin Panel - User Guide

## Table of Contents

1. [Introduction](#introduction)
2. [Getting Started](#getting-started)
3. [Dashboard](#dashboard)
4. [Tenant Management](#tenant-management)
5. [Analytics](#analytics)
6. [Audit Logs](#audit-logs)
7. [Settings](#settings)
8. [Best Practices](#best-practices)
9. [FAQ](#faq)

## Introduction

Welcome to the SAAS Manager Admin Panel! This comprehensive guide will help you understand and effectively use all features of the platform to manage your multi-tenant clinic management system.

### What is the SAAS Manager Admin Panel?

The SAAS Manager Admin Panel is a centralized management interface that allows you to:

- **Create and manage tenant organizations** (clinics)
- **Monitor system health** and performance metrics
- **Configure tenant branding** and settings
- **View analytics** and usage reports
- **Track administrative actions** through audit logs
- **Receive notifications** for critical events

### Who Should Use This Guide?

This guide is designed for SAAS managers and administrators who are responsible for:
- Onboarding new clinic tenants
- Managing existing tenant configurations
- Monitoring system performance
- Troubleshooting tenant issues
- Generating reports and analytics

## Getting Started

### Logging In

1. Navigate to the SAAS Manager Admin Panel URL (e.g., `https://saas-admin.yourdomain.com`)
2. Enter your SAAS manager email address
3. Enter your password
4. Click "Sign In"

**Note**: If you don't have login credentials, contact your system administrator.

### First-Time Login

After your first login, you should:

1. **Familiarize yourself with the dashboard** - Review the system metrics and recent activity
2. **Check system health** - Ensure all systems are operational
3. **Review existing tenants** - Navigate to the Tenants page to see current organizations
4. **Set your language preference** - Click your name in the top-right corner to change language (English/Arabic)

### Navigation

The main navigation is located in the left sidebar (desktop) or hamburger menu (mobile):

- **Dashboard** - System overview and metrics
- **Tenants** - Manage tenant organizations
- **Analytics** - View system-wide analytics and reports
- **Audit Logs** - Track administrative actions

### User Menu

Click your name in the top-right corner to access:
- **Language Switcher** - Change between English and Arabic
- **Logout** - Sign out of the admin panel

## Dashboard

The Dashboard provides a real-time overview of your entire system.

### System Metrics

The dashboard displays key metrics in card format:

#### Total Tenants
- Shows the total number of tenant organizations
- Includes both active and inactive tenants
- Trend indicator shows growth over the last 30 days

#### Active Tenants
- Shows tenants currently in active status
- Excludes inactive or deleted tenants
- Percentage of total tenants displayed

#### Total Users
- Shows all users across all tenants
- Includes staff, doctors, and patients
- Trend indicator shows user growth

#### Active Users
- Shows users who have logged in within the last 30 days
- Helps gauge platform engagement
- Percentage of total users displayed

### System Health Widget

The System Health Widget shows the operational status of critical components:

- **API Response Time** - Average response time for API calls
  - Green: < 200ms (Healthy)
  - Yellow: 200-500ms (Degraded)
  - Red: > 500ms (Critical)

- **Database Status** - Database connection and performance
  - Green: Healthy
  - Yellow: Degraded
  - Red: Down

- **Storage Usage** - Disk space utilization
  - Shows percentage used
  - Warning when > 80%

### Recent Activity Feed

Shows the latest administrative actions:
- Tenant creation
- Tenant updates
- Tenant deletions
- Status changes

Each activity entry shows:
- Action type and description
- Manager who performed the action
- Timestamp
- Affected tenant

### Auto-Refresh

The dashboard automatically refreshes metrics every 30 seconds to provide real-time data.

## Tenant Management

### Viewing Tenants

Navigate to **Tenants** in the sidebar to see all tenant organizations.

#### Tenant List Features

**Search**
- Type in the search box to filter tenants by name or slug
- Search is debounced (300ms delay) for better performance
- Clear search to show all tenants

**Filter by Status**
- **All** - Show all tenants
- **Active** - Show only active tenants
- **Inactive** - Show only inactive tenants
- **Deleted** - Show soft-deleted tenants

**Sort**
- Click column headers to sort:
  - Name (A-Z or Z-A)
  - Creation Date (newest or oldest first)
  - Status (alphabetically)

**Pagination**
- Use pagination controls at the bottom
- Change page size (10, 20, 50, 100 items per page)
- Navigate between pages

#### Tenant Information Displayed

Each tenant row shows:
- **Name** - Display name of the clinic
- **Slug** - Unique identifier (used in URLs)
- **Custom Domain** - Custom domain if configured
- **Status** - Active, Inactive, or Deleted
- **Created** - Date the tenant was created

### Creating a New Tenant

1. Click the **"Create Tenant"** button (top-right of Tenants page)
2. Fill in the required information:

   **Slug** (Required)
   - Unique identifier for the tenant
   - Used in URLs and system references
   - Must be lowercase, alphanumeric, and hyphens only
   - Example: `abc-clinic`, `downtown-dental`
   - Real-time validation checks for uniqueness

   **Name** (Required)
   - Display name of the clinic
   - Can include spaces and special characters
   - Example: `ABC Clinic`, `Downtown Dental Center`

   **Custom Domain** (Optional)
   - Custom domain for the tenant's public website
   - Example: `abcclinic.com`
   - Must be a valid domain format

3. Click **"Create Tenant"**

4. **Important**: A success modal will display the admin credentials:
   - **Admin Email** - Auto-generated based on slug
   - **Admin Password** - Randomly generated secure password
   - **Copy both credentials** - Use the copy buttons
   - **Save credentials securely** - They are shown only once!

5. Click **"View Tenant Details"** to see the new tenant

**Best Practices for Tenant Creation:**
- Use descriptive slugs that identify the clinic
- Keep slugs short but meaningful
- Document admin credentials immediately
- Send credentials to the clinic administrator securely
- Verify the tenant can log in before closing the ticket

### Viewing Tenant Details

Click on any tenant in the list to view detailed information:

#### Tenant Information Section
- Name, slug, and custom domain
- Status badge (Active/Inactive/Deleted)
- Creation and last update timestamps
- Contact information (if available)

#### Tenant Metrics
- **Users** - Total user count (staff + patients)
- **Staff** - Number of staff members
- **Patients** - Number of registered patients
- **Appointments** - Total appointments created
- **Storage** - Storage space used (in MB)
- **Last Activity** - Most recent user activity

#### Action Buttons
- **Edit** - Modify tenant information
- **Configure Branding** - Customize tenant appearance
- **Delete** - Remove tenant (with confirmation)

#### Recent Activity
Shows recent actions related to this tenant:
- Configuration changes
- Status updates
- Branding modifications

### Editing a Tenant

1. Navigate to the tenant detail page
2. Click **"Edit"** button
3. Modify the information:
   - **Name** - Update display name
   - **Custom Domain** - Add or change custom domain
   - **Status** - Toggle between Active and Inactive

4. Click **"Save Changes"**

**Status Changes:**
- **Active → Inactive**: Tenant users cannot log in, but data is preserved
- **Inactive → Active**: Tenant users can log in again
- A confirmation dialog appears for status changes

### Configuring Tenant Branding

Customize the appearance of a tenant's admin panel:

1. Navigate to the tenant detail page
2. Click **"Configure Branding"**
3. Use the visual editor to customize:

   **Primary Color**
   - Main brand color used for buttons, links, and accents
   - Click the color picker to choose a color
   - Enter hex code directly (e.g., `#3B82F6`)

   **Secondary Color**
   - Complementary color for secondary elements
   - Used for hover states and secondary buttons

   **Logo URL**
   - URL to the tenant's logo image
   - Recommended size: 200x50px
   - Supported formats: PNG, JPG, SVG

4. **Live Preview** - See changes in real-time in the preview panel

5. Click **"Save Branding"** to apply changes

6. Click **"Reset"** to revert to original values

**Branding Best Practices:**
- Use high-contrast colors for accessibility
- Test colors with the preview panel
- Use web-safe colors (hex format)
- Ensure logo has transparent background (PNG)
- Keep logo file size under 100KB for performance

### Deleting a Tenant

**Warning**: Tenant deletion is a serious action. Deleted tenants cannot log in, but their data is archived for compliance.

1. Navigate to the tenant detail page
2. Click **"Delete"** button
3. Read the confirmation dialog carefully:
   - Tenant users will lose access immediately
   - Data will be archived (not permanently deleted)
   - Action is logged in audit trail
   - Deletion can be reversed by support team if needed

4. Type the tenant slug to confirm
5. Click **"Confirm Delete"**

**When to Delete a Tenant:**
- Tenant has requested account closure
- Tenant has not paid and grace period expired
- Tenant is migrating to another platform
- Duplicate or test tenant needs cleanup

**After Deletion:**
- Tenant status changes to "Deleted"
- Tenant users cannot log in
- Data is retained for compliance (typically 90 days)
- Audit log entry is created
- You can view deleted tenants by filtering for "Deleted" status

## Analytics

The Analytics page provides insights into system usage and growth.

### Time Range Selection

Choose a time range for analytics data:
- **Last 7 Days** - Recent activity
- **Last 30 Days** - Monthly trends
- **Last 90 Days** - Quarterly overview
- **Custom Range** - Select specific start and end dates

### Tenant Growth Chart

Line chart showing tenant growth over time:
- **Total Tenants** - Cumulative tenant count
- **New Tenants** - New tenants added per period
- Hover over data points for exact values
- Zoom in/out using chart controls

### Usage Metrics Chart

Bar chart showing system usage:
- **Active Users** - Users who logged in during the period
- **Appointments Created** - New appointments per period
- **API Calls** - Total API requests
- Compare metrics across different time periods

### Resource Usage Statistics

Table showing resource consumption:
- **Database Size** - Total database storage used
- **File Storage** - Total file storage used
- **API Call Volume** - Total API requests
- **Average Response Time** - API performance metric

### Exporting Analytics

1. Select your desired time range
2. Click **"Export to PDF"** button
3. PDF report will be generated and downloaded
4. Report includes:
   - All charts and graphs
   - Summary statistics
   - Time range and generation date
   - Your name as the report generator

**Use Cases for Analytics:**
- Monthly reports for stakeholders
- Capacity planning and resource allocation
- Identifying growth trends
- Performance monitoring
- Billing and usage tracking

## Audit Logs

The Audit Logs page provides a complete history of administrative actions.

### Viewing Audit Logs

Navigate to **Audit Logs** in the sidebar to see all logged actions.

### Filtering Audit Logs

**Date Range Filter**
- Click the date range picker
- Select start and end dates
- Click "Apply" to filter logs

**Action Type Filter**
- Select from dropdown:
  - All Actions
  - Tenant Created
  - Tenant Updated
  - Tenant Deleted
  - Status Changed
  - Branding Updated

**Manager Filter**
- Filter by specific SAAS manager
- Shows only actions by that manager

**Tenant Filter**
- Filter by specific tenant
- Shows only actions affecting that tenant

### Audit Log Information

Each log entry shows:
- **Timestamp** - When the action occurred
- **Manager** - Who performed the action
- **Action** - Type of action performed
- **Tenant** - Which tenant was affected
- **Details** - Additional information about the action

### Exporting Audit Logs

1. Apply desired filters
2. Click **"Export to CSV"** button
3. CSV file will be downloaded
4. Open in Excel, Google Sheets, or other spreadsheet software

**CSV Contents:**
- All columns from the audit log table
- Filtered results based on your current filters
- Timestamp in ISO format for easy sorting

**Use Cases for Audit Logs:**
- Compliance and regulatory requirements
- Security investigations
- Troubleshooting issues
- Performance reviews
- Change tracking

## Settings

### Language Settings

Change the interface language:

1. Click your name in the top-right corner
2. Click **"Language"** in the dropdown
3. Select **English** or **Arabic**
4. Interface updates immediately

**Arabic Language Features:**
- Full RTL (right-to-left) layout
- All text translated to Arabic
- Date and number formatting adjusted
- Maintains all functionality

### Session Management

Your session is managed automatically:

- **Session Duration** - 30 minutes of inactivity
- **Warning Dialog** - Appears 5 minutes before timeout
- **Auto Logout** - Occurs after timeout expires
- **Manual Logout** - Click your name → Logout

**Session Security:**
- JWT token stored securely in browser
- Token validated on every page navigation
- Automatic logout on token expiration
- Automatic logout on 401 API responses

## Best Practices

### Tenant Management

1. **Document Everything**
   - Save admin credentials immediately after tenant creation
   - Document custom configurations
   - Keep notes on tenant-specific requirements

2. **Regular Monitoring**
   - Check dashboard daily for system health
   - Review tenant metrics weekly
   - Monitor storage usage to prevent capacity issues

3. **Proactive Communication**
   - Notify tenants before making changes
   - Provide advance notice for maintenance
   - Send credentials securely (never via email)

4. **Security**
   - Use strong, unique passwords
   - Log out when leaving your workstation
   - Review audit logs regularly for suspicious activity
   - Report security concerns immediately

5. **Branding Configuration**
   - Test branding changes in preview before saving
   - Keep backup of original branding values
   - Ensure colors meet accessibility standards
   - Optimize logo images for web

### Performance Optimization

1. **Use Filters Effectively**
   - Filter tenant lists to reduce load times
   - Use pagination for large datasets
   - Apply date ranges in analytics to limit data

2. **Browser Maintenance**
   - Clear browser cache periodically
   - Use modern browsers (Chrome, Firefox, Safari)
   - Keep browser updated to latest version
   - Disable unnecessary browser extensions

3. **Network Considerations**
   - Use stable internet connection
   - Avoid VPNs that may slow connections
   - Report persistent slow performance

### Data Management

1. **Regular Backups**
   - Ensure backend backups are running
   - Verify backup integrity periodically
   - Document backup and restore procedures

2. **Data Retention**
   - Understand data retention policies
   - Archive old audit logs as needed
   - Clean up test/demo tenants regularly

3. **Compliance**
   - Follow data protection regulations (GDPR, HIPAA, etc.)
   - Document tenant data handling procedures
   - Maintain audit logs for required retention period

## FAQ

### General Questions

**Q: How do I reset my password?**
A: Contact your system administrator. Password reset functionality is managed at the backend level.

**Q: Can I manage multiple tenants simultaneously?**
A: Yes, you can switch between tenants by navigating to different tenant detail pages. Each tenant is managed independently.

**Q: What happens if I accidentally delete a tenant?**
A: Deleted tenants are soft-deleted (archived). Contact your system administrator to restore a deleted tenant if needed within the retention period.

**Q: How often is the dashboard updated?**
A: The dashboard automatically refreshes every 30 seconds. You can also manually refresh by reloading the page.

### Tenant Management

**Q: What is a tenant slug?**
A: A slug is a unique identifier for a tenant, used in URLs and system references. It must be lowercase, alphanumeric, and can include hyphens.

**Q: Can I change a tenant's slug after creation?**
A: No, slugs cannot be changed after creation as they are used throughout the system. Choose carefully during creation.

**Q: What happens when I set a tenant to inactive?**
A: Inactive tenants cannot log in, but their data is preserved. You can reactivate them at any time.

**Q: How do I recover lost admin credentials?**
A: Admin credentials are shown only once during creation. If lost, contact your system administrator to reset the tenant admin password.

**Q: Can a tenant have multiple custom domains?**
A: Currently, each tenant can have one custom domain. Contact development team if multiple domains are needed.

### Branding

**Q: What image formats are supported for logos?**
A: PNG, JPG, and SVG formats are supported. PNG with transparent background is recommended.

**Q: How long does it take for branding changes to apply?**
A: Branding changes apply immediately after saving. Tenant users may need to refresh their browser to see changes.

**Q: Can I preview branding before applying?**
A: Yes, the branding editor includes a live preview panel that shows changes in real-time before saving.

### Analytics and Reporting

**Q: How far back does analytics data go?**
A: Analytics data is available from the system launch date. Use the custom date range to access historical data.

**Q: Can I schedule automatic reports?**
A: Automatic report scheduling is not currently available. You can manually export reports as needed.

**Q: What timezone are timestamps shown in?**
A: Timestamps are shown in your browser's local timezone. Exported data includes timezone information.

### Technical Issues

**Q: The page is loading slowly. What should I do?**
A: Try clearing your browser cache, checking your internet connection, and ensuring the backend API is responsive. See the Troubleshooting section in the main README.

**Q: I'm getting authentication errors. What should I do?**
A: Try logging out and logging back in. If the issue persists, clear your browser's localStorage and try again. Contact support if the problem continues.

**Q: The interface is not displaying correctly. What should I do?**
A: Ensure you're using a modern browser (Chrome, Firefox, Safari). Try clearing cache and hard reloading (Ctrl+Shift+R or Cmd+Shift+R).

### Support

**Q: Who do I contact for technical support?**
A: Contact your system administrator or development team. Include relevant error messages and screenshots when reporting issues.

**Q: How do I request new features?**
A: Submit feature requests to your development team or product manager. Include detailed use cases and business justification.

**Q: Where can I find more documentation?**
A: Additional documentation is available in the `docs/` directory of the application. See the main README for a complete list.

---

## Need More Help?

If you have questions not covered in this guide:

1. Check the [Troubleshooting section](../README.md#troubleshooting) in the main README
2. Review the [Additional Documentation](../README.md#additional-documentation)
3. Contact your system administrator
4. Submit a support ticket with detailed information about your issue

---

**Document Version**: 1.0  
**Last Updated**: 2024  
**Maintained By**: Development Team
