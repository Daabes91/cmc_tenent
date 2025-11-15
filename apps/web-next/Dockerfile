# syntax=docker/dockerfile:1

ARG NEXT_PUBLIC_API_URL
ARG NEXT_SERVER_API_URL
ARG NEXT_SERVER_API_BASE_URL
ARG DATABASE_URL=postgresql://clinic:clinic_password@postgres:5432/clinic_multi_tenant

FROM node:20-alpine AS deps
RUN apk add --no-cache tar xz gzip
WORKDIR /app
COPY package.json .
RUN corepack enable && corepack prepare pnpm@10.22.0 --activate && pnpm install --no-frozen-lockfile

FROM node:20-alpine AS builder
ARG NEXT_PUBLIC_API_URL
ARG NEXT_SERVER_API_URL
ARG NEXT_SERVER_API_BASE_URL
ARG DATABASE_URL
RUN apk add --no-cache tar xz gzip
RUN corepack enable && corepack prepare pnpm@10.22.0 --activate
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ENV NEXT_SERVER_API_URL=${NEXT_SERVER_API_URL}
ENV NEXT_SERVER_API_BASE_URL=${NEXT_SERVER_API_BASE_URL}
ENV DATABASE_URL=${DATABASE_URL}
ENV NODE_OPTIONS=--max-old-space-size=4096
WORKDIR /app
COPY --from=deps /app/node_modules ./node_modules
COPY . .
RUN pnpm install --no-frozen-lockfile && npx prisma generate && pnpm run build

FROM node:20-alpine AS runner
ARG NEXT_PUBLIC_API_URL
ARG NEXT_SERVER_API_URL
ARG NEXT_SERVER_API_BASE_URL
ARG DATABASE_URL
ENV NEXT_PUBLIC_API_URL=${NEXT_PUBLIC_API_URL}
ENV NEXT_SERVER_API_URL=${NEXT_SERVER_API_URL}
ENV NEXT_SERVER_API_BASE_URL=${NEXT_SERVER_API_BASE_URL}
ENV DATABASE_URL=${DATABASE_URL}
WORKDIR /app
ENV NODE_ENV=production
COPY --from=builder /app/.next ./.next
COPY --from=builder /app/public ./public
COPY --from=builder /app/package.json ./package.json
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/prisma ./prisma
EXPOSE 3001
CMD ["npm", "run", "start"]
