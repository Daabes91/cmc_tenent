# Build stage
FROM node:20-alpine AS builder

WORKDIR /app

# Enable Corepack and install pnpm
RUN corepack enable && corepack prepare pnpm@8.15.0 --activate

# Build arguments
ARG NEXT_PUBLIC_API_BASE_URL
ARG NEXT_PUBLIC_APP_URL
ARG NEXT_PUBLIC_PAYPAL_CLIENT_ID
ARG NEXT_PUBLIC_BASE_PATH
ARG NEXT_PUBLIC_ADMIN_URL
ARG NEXT_PUBLIC_PAYMENT_PROVIDER
ARG NEXT_PUBLIC_CLINIC_ADMIN_URL
ARG NEXT_PUBLIC_SAAS_ADMIN_URL

# Set environment variables for build
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL
ENV NEXT_PUBLIC_APP_URL=$NEXT_PUBLIC_APP_URL
ENV NEXT_PUBLIC_PAYPAL_CLIENT_ID=$NEXT_PUBLIC_PAYPAL_CLIENT_ID
ENV NEXT_PUBLIC_BASE_PATH=$NEXT_PUBLIC_BASE_PATH
ENV NEXT_PUBLIC_ADMIN_URL=$NEXT_PUBLIC_ADMIN_URL
ENV NEXT_PUBLIC_PAYMENT_PROVIDER=$NEXT_PUBLIC_PAYMENT_PROVIDER
ENV NEXT_PUBLIC_CLINIC_ADMIN_URL=$NEXT_PUBLIC_CLINIC_ADMIN_URL
ENV NEXT_PUBLIC_SAAS_ADMIN_URL=$NEXT_PUBLIC_SAAS_ADMIN_URL

# Copy package files
COPY package.json ./

# Install dependencies (no lockfile, will generate new one)
RUN pnpm install --no-frozen-lockfile

# Copy source code
COPY . .

# Build the application
RUN pnpm build

# Production stage
FROM node:20-alpine AS runner

WORKDIR /app

# Enable Corepack and install pnpm
RUN corepack enable && corepack prepare pnpm@8.15.0 --activate

# Re-declare build arguments for runtime env injection
ARG NEXT_PUBLIC_API_BASE_URL
ARG NEXT_PUBLIC_APP_URL
ARG NEXT_PUBLIC_PAYPAL_CLIENT_ID
ARG NEXT_PUBLIC_BASE_PATH
ARG NEXT_PUBLIC_ADMIN_URL
ARG NEXT_PUBLIC_PAYMENT_PROVIDER
ARG NEXT_PUBLIC_CLINIC_ADMIN_URL
ARG NEXT_PUBLIC_SAAS_ADMIN_URL

# Persist environment variables for runtime (used by Next.js server)
ENV NEXT_PUBLIC_API_BASE_URL=$NEXT_PUBLIC_API_BASE_URL
ENV NEXT_PUBLIC_APP_URL=$NEXT_PUBLIC_APP_URL
ENV NEXT_PUBLIC_PAYPAL_CLIENT_ID=$NEXT_PUBLIC_PAYPAL_CLIENT_ID
ENV NEXT_PUBLIC_BASE_PATH=$NEXT_PUBLIC_BASE_PATH
ENV NEXT_PUBLIC_ADMIN_URL=$NEXT_PUBLIC_ADMIN_URL
ENV NEXT_PUBLIC_PAYMENT_PROVIDER=$NEXT_PUBLIC_PAYMENT_PROVIDER
ENV NEXT_PUBLIC_CLINIC_ADMIN_URL=$NEXT_PUBLIC_CLINIC_ADMIN_URL
ENV NEXT_PUBLIC_SAAS_ADMIN_URL=$NEXT_PUBLIC_SAAS_ADMIN_URL

# Copy necessary files from builder
COPY --from=builder /app/package.json ./
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/next.config.js ./
COPY --from=builder /app/node_modules ./node_modules

# Set environment to production
ENV NODE_ENV=production
ENV PORT=3003

# Expose port
EXPOSE 3003

# Start the application
CMD ["pnpm", "start", "-p", "3003"]
