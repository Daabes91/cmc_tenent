# syntax=docker/dockerfile:1

ARG NEXT_PUBLIC_API_URL
ARG NEXT_SERVER_API_URL
ARG NEXT_SERVER_API_BASE_URL
ARG DATABASE_URL=postgresql://clinic:clinic_password@postgres:5432/clinic_multi_tenant

FROM node:20-bookworm-slim AS deps
RUN apt-get update && apt-get install -y --no-install-recommends xz-utils openssl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
COPY package.json .
RUN corepack enable && corepack prepare pnpm@10.22.0 --activate && pnpm install --no-frozen-lockfile

FROM node:20-bookworm-slim AS builder
ARG NEXT_PUBLIC_API_URL
ARG NEXT_SERVER_API_URL
ARG NEXT_SERVER_API_BASE_URL
ARG DATABASE_URL
RUN apt-get update && apt-get install -y --no-install-recommends openssl && rm -rf /var/lib/apt/lists/* \
    && corepack enable && corepack prepare pnpm@10.22.0 --activate
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ENV NEXT_SERVER_API_URL=${NEXT_SERVER_API_URL}
ENV NEXT_SERVER_API_BASE_URL=${NEXT_SERVER_API_BASE_URL}
ENV DATABASE_URL=${DATABASE_URL}
ENV NODE_OPTIONS=--max-old-space-size=4096
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN pnpm install --no-frozen-lockfile \
    && pnpm --filter web-next run build

FROM node:20-bookworm-slim AS runner
ARG NEXT_PUBLIC_API_URL
ARG NEXT_SERVER_API_URL
ARG NEXT_SERVER_API_BASE_URL
ARG DATABASE_URL
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ENV NEXT_SERVER_API_URL=${NEXT_SERVER_API_URL}
ENV NEXT_SERVER_API_BASE_URL=${NEXT_SERVER_API_BASE_URL}
ENV DATABASE_URL=${DATABASE_URL}
RUN apt-get update && apt-get install -y --no-install-recommends openssl && rm -rf /var/lib/apt/lists/*
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/package.json ./package.json
RUN npm install --omit=dev
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/next.config.ts ./next.config.ts
EXPOSE 3001
CMD ["npm", "run", "start"]
