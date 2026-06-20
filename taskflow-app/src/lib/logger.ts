type LogLevel = 'info' | 'warn' | 'error';

interface LogEntry {
  level: LogLevel;
  message: string;
  timestamp: string;
  [key: string]: unknown;
}

function log(level: LogLevel, message: string, context: Record<string, unknown> = {}): void {
  const entry: LogEntry = {
    ...context,
    level,
    message,
    timestamp: new Date().toISOString(),
  };
  console[level](JSON.stringify(entry));
}

export const logger = {
  info:  (message: string, context?: Record<string, unknown>) => log('info',  message, context),
  warn:  (message: string, context?: Record<string, unknown>) => log('warn',  message, context),
  error: (message: string, context?: Record<string, unknown>) => log('error', message, context),
};
